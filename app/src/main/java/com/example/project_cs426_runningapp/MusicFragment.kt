package com.example.project_cs426_runningapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.example.project_cs426_runningapp.databinding.FragmentMusicBinding

class MusicFragment : Fragment() {
    private val clientID = "12b274aab0934f67942cba17bd6770c7"
    private val redirectUri = "http://www.cs426.com/redirect"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var binding: FragmentMusicBinding
    private val stateTrack: Int = 1 // 0: paused, 1: playing

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
                //TODO: REMOVE THIS WHEN DONE DEVELOPING
                spotifyAppRemote?.playerApi?.play("spotify:track:4cOdK2wGLETKBW3PvgPWqT")
                subscribeToPlayerState()
            }
            override fun onFailure(throwable: Throwable?) {
                if (throwable != null) {

                }
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
            if (track != null) {
                updateUI(track)
            }
        }
    }

    private fun updateUI(track: com.spotify.protocol.types.Track) {
        binding.trackNameTextView.text = track.name
        binding.artistNameTextView.text = track.artist.name
        spotifyAppRemote?.imagesApi?.getImage(track.imageUri)
            ?.setResultCallback { bitmap ->
                binding.albumCoverImageView.setImageBitmap(bitmap)
            }
        if (stateTrack == 0) {
            binding.playerPause.visibility = View.VISIBLE
            binding.playerContinue.visibility = View.INVISIBLE
        } else {
            binding.playerPause.visibility = View.INVISIBLE
            binding.playerContinue.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToPlayerState()
        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.player_back -> spotifyAppRemote?.playerApi?.skipPrevious()
                R.id.player_skip -> spotifyAppRemote?.playerApi?.skipNext()
                R.id.control_circle -> {
                    if (stateTrack == 0) {
                        spotifyAppRemote?.playerApi?.resume()
                        stateTrack == 1
                    } else {
                        spotifyAppRemote?.playerApi?.pause()
                        stateTrack == 0
                    }
                }
            }
        }
        binding.playerSkip.setOnClickListener(clickListener)
        binding.playerBack.setOnClickListener(clickListener)
        binding.controlCircle.setOnClickListener(clickListener)
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