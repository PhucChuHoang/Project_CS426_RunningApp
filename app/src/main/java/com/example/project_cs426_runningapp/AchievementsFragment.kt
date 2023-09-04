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
import com.example.project_cs426_runningapp.databinding.FragmentAchievementsBinding

class AchievementsFragment : Fragment() {
    private val clientID = "12b274aab0934f67942cba17bd6770c7"
    private val redirectUri = "http://www.cs426.com/redirect"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var binding: FragmentAchievementsBinding

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(clientID)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()
        SpotifyAppRemote.connect(requireContext(), connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote?) {
                spotifyAppRemote = appRemote
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
        binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun subscribeToPlayerState() {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            val track = playerState.track
            if (track != null) {
                spotifyAppRemote?.imagesApi?.getImage(track.imageUri)
                    ?.setResultCallback { bitmap ->
                        binding.albumCoverImageView.setImageBitmap(bitmap)
                    }
                updateUI(track.name, track.artist.name)
            }
        }
    }

    private fun updateUI(trackName: String, artistName: String) {
        binding.trackNameTextView.text = trackName
        binding.artistNameTextView.text = artistName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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