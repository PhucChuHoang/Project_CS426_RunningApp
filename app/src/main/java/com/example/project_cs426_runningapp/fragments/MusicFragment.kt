package com.example.project_cs426_runningapp.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentMusicBinding
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.example.project_cs426_runningapp.other.Constants.SPOTIFY_CLIENT_ID as clientID
import com.example.project_cs426_runningapp.other.Constants.SPOTIFY_REDIRECT_URI as redirectUri

class MusicFragment : Fragment() {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var binding: FragmentMusicBinding
    private var volumeSeekBar: SeekBar? = null

    override fun onStart() {
        super.onStart()
        if (isSpotifyInstalled(requireContext())) {
            val connectionParams = ConnectionParams.Builder(clientID)
                .setRedirectUri(redirectUri)
                .showAuthView(true)
                .build()
            SpotifyAppRemote.connect(context, connectionParams, connectionListener)
        } else {
            handleSpotifyNotInstalled()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun isSpotifyInstalled(context: Context): Boolean {
        val spotifyPackageName = "com.spotify.music"
        try {
            context.packageManager.getPackageInfo(spotifyPackageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    private val connectionListener = object : Connector.ConnectionListener {
        override fun onConnected(appRemote: SpotifyAppRemote?) {
            spotifyAppRemote = appRemote
            subscribeToPlayerState()
        }

        override fun onFailure(p0: Throwable?) {
            handleSpotifyNotInstalled()
        }
    }

    private fun handleSpotifyNotInstalled() {
        Log.d("MusicFragment", "Spotify not installed")
        binding.spotifyNotInstalledTextView.visibility = View.VISIBLE
        binding.playerSkip.visibility = View.GONE
        binding.playerBack.visibility = View.GONE
        binding.controlCircle.visibility = View.GONE
        binding.trackNameTextView.visibility = View.GONE
        binding.artistNameTextView.visibility = View.GONE
        binding.albumCoverImageView.visibility = View.GONE
        binding.seekBar.visibility = View.GONE
        binding.playerPause.visibility = View.GONE
    }

    private fun subscribeToPlayerState() {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            val track = playerState.track
            val isPlaying = playerState.isPaused.not()
            if (track != null) {
                updateUI(track, isPlaying)
            }
        }
    }

    private fun updateUI(track: com.spotify.protocol.types.Track, isPlaying: Boolean) {
        Log.d("Check", "updateUI: ${track.name}")
        binding.trackNameTextView.text = track.name
        binding.artistNameTextView.text = track.artist.name
        spotifyAppRemote?.imagesApi?.getImage(track.imageUri)
            ?.setResultCallback { bitmap ->
                val palette = Palette.from(bitmap).generate()
                val dominantColor = palette.getDominantColor(Color.WHITE)

                // Set the background color of MusicBackGround
                binding.MusicBackGround.background = createGradientBackground(dominantColor)

                // Set the album cover image
                binding.albumCoverImageView.setImageBitmap(bitmap)
            }
        if (isPlaying) {
            binding.playerContinue.visibility = View.GONE
            binding.playerPause.visibility = View.VISIBLE
        }
        else {
            binding.playerContinue.visibility = View.VISIBLE
            binding.playerPause.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize volumeSeekBar
        volumeSeekBar = view.findViewById(R.id.seekBar)
        volumeSeekBar?.progress?.let { spotifyAppRemote?.connectApi?.connectSetVolume(it.toFloat()) }
        // Set a listener to handle volume changes
        volumeSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Calculate the volume based on the SeekBar progress
                    val volume = progress / 100f
                    // Set the Spotify volume using the SpotifyAppRemote
                    spotifyAppRemote?.connectApi?.connectSetVolume(volume)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No action needed when tracking starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No action needed when tracking stops
            }
        })

        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.player_back -> {
                    spotifyAppRemote?.playerApi?.skipPrevious()
                }
                R.id.player_skip -> {
                    spotifyAppRemote?.playerApi?.skipNext()
                }
                R.id.control_circle -> {
                    spotifyAppRemote?.playerApi?.playerState?.setResultCallback { playerState ->
                        val isPlaying = playerState.isPaused.not()
                        if (isPlaying) {
                            spotifyAppRemote?.playerApi?.pause()
                        } else {
                            spotifyAppRemote?.playerApi?.resume()
                        }
                    }
                }
            }
        }
        binding.playerSkip.setOnClickListener(clickListener)
        binding.playerBack.setOnClickListener(clickListener)
        binding.controlCircle.setOnClickListener(clickListener)
        subscribeToPlayerState()
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
    private fun createGradientBackground(dominantColor: Int): GradientDrawable {
        val luminance = calculateLuminance(dominantColor)
        val middleColor = calculateMiddleColor(luminance)

        val gradientColors = intArrayOf(dominantColor, middleColor, dominantColor)

        val contrastColor = getContrastColor(middleColor)
        // Set text colors with contrast to the dominant color
        binding.trackNameTextView.setTextColor(contrastColor)
        binding.artistNameTextView.setTextColor(contrastColor)

        return GradientDrawable().apply {
            colors = gradientColors
            gradientType = GradientDrawable.LINEAR_GRADIENT
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
        }
    }

    private fun calculateLuminance(color: Int): Double {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0
        return 0.299 * r + 0.587 * g + 0.114 * b
    }

    private fun calculateMiddleColor(luminance: Double): Int {
        // Calculate the middle color based on luminance
        val middleLuminance = if (luminance > 0.5) 0.2 else 0.8
        return ColorUtils.blendARGB(
            Color.BLACK,
            Color.WHITE,
            middleLuminance.toFloat()
        )
    }
    private fun getContrastColor(dominantColor: Int): Int {
        // Calculate the luminance of the dominant color
        val r = Color.red(dominantColor)
        val g = Color.green(dominantColor)
        val b = Color.blue(dominantColor)
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255

        // Determine the contrast color based on luminance
        return if (luminance > 0.5) {
            // Use black for light backgrounds
            Color.BLACK
        } else {
            // Use white for dark backgrounds
            Color.WHITE
        }
    }
}