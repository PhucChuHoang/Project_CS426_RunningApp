package com.example.project_cs426_runningapp

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.example.project_cs426_runningapp.databinding.FragmentMusicBinding
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote

class MusicFragment : Fragment() {
    private val clientID = "12b274aab0934f67942cba17bd6770c7"
    private val redirectUri = "http://www.cs426.com/redirect"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var binding: FragmentMusicBinding

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(clientID)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()
        SpotifyAppRemote.connect(requireContext(), connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote?) {
                spotifyAppRemote = appRemote
                Log.d("MusicFragment", "Connected! Yay!")
                subscribeToPlayerState()
            }

            override fun onFailure(p0: Throwable?) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root
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

        val gradientColors = if (luminance > 0.5) {
            // If the dominant color is light, transition to white
            intArrayOf(dominantColor, middleColor, Color.WHITE)
        } else {
            // If the dominant color is dark, transition to black
            intArrayOf(dominantColor, middleColor, Color.BLACK)
        }


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
        val interpolatedColor = ColorUtils.blendARGB(
            Color.BLACK,
            Color.WHITE,
            middleLuminance.toFloat()
        )
        return interpolatedColor
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