package com.example.project_cs426_runningapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project_cs426_runningapp.R
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.example.project_cs426_runningapp.databinding.FragmentMusicBinding

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
}