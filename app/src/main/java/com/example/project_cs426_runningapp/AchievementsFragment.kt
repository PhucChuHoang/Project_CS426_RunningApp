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
import com.spotify.protocol.types.Track

class AchievementsFragment : Fragment() {
    private val clientID = "12b274aab0934f67942cba17bd6770c7"
    private val redirectUri = "http://www.cs426.com/redirect"
    private var spotifyAppRemote: SpotifyAppRemote? = null

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(clientID)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()
        SpotifyAppRemote.connect(requireContext(), connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote?) {
                spotifyAppRemote = appRemote
                Log.d("MainActivity", "Connected! Yay!")
                spotifyAppRemote?.let {
                    val playlistURI = "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL"
                    it.playerApi.play(playlistURI)
                    // Subscribe to PlayerState
                    it.playerApi.subscribeToPlayerState().setEventCallback {
                        val track: Track = it.track
                        Log.d("MainActivity", track.name + " by " + track.artist.name)
                    }
                }
            }
            override fun onFailure(throwable: Throwable?) {
                if (throwable != null) {
                    Log.e("MainActivity", throwable.message, throwable)
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_achievements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}