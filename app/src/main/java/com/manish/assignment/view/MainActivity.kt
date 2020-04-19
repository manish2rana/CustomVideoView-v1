package com.manish.assignment.view

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.manish.assignment.R
import com.manish.assignment.model.Video
import com.manish.assignment.utils.CustomMediaController
import com.manish.assignment.utils.Utility
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
    CustomMediaController.MediaPlayerControl {

    private val adapter: CustomListAdapter = CustomListAdapter(ArrayList<Video>())

    lateinit var listData: List<Video>
    var player: MediaPlayer? = null
    var controller: CustomMediaController? = null

    lateinit var recyclerView: RecyclerView

    var positionSelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.vList)
        setDefaultHeight()
        readJsonData()
        val videoHolder = videoSurface.holder
        videoHolder.addCallback(this)

        player = MediaPlayer()
        controller = CustomMediaController(this)
        val videoController = MediaController(this)
        try {
            player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player?.setDataSource(
                this,
                Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
            )
            player?.setOnPreparedListener(this)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // listener for next and previous video
        controller?.setPrevNextListeners(View.OnClickListener {
            player?.reset()
            if (positionSelected < listData.size)
                positionSelected += 1
            player?.setDataSource(
                this,
                Uri.parse(listData[positionSelected].sources[0])
            )
            player?.prepare()
            player?.start()
        },
            View.OnClickListener {
                player?.reset()
                if (positionSelected != 0)
                    positionSelected -= 1
                player?.setDataSource(
                    this,
                    Uri.parse(listData[positionSelected].sources[0])
                )
                player?.prepare()
                player?.start()
            })
    }


    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {

    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        player?.setDisplay(holder)
        player?.prepareAsync()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        controller?.setMediaPlayer(this)
        controller?.setAnchorView(findViewById<FrameLayout>(R.id.videoSurfaceContainer))
        player?.start()
        controller?.isEnabled = true

    }

    override fun start() {
        if (player != null) {
            return player?.start()!!
        }
    }

    override fun pause() {
        if (player != null) {
            return player?.pause()!!
        }
    }

    override fun getDuration(): Int {
        if (player != null) {
            return player?.duration!!
        } else {
            return 0
        }
    }

    override fun getCurrentPosition(): Int {
        if (player != null) {
            return player?.getCurrentPosition()!!
        } else {
            return 0
        }
    }

    override fun seekTo(pos: Int) {
        if (player != null) {
            return player?.seekTo(pos)!!
        }
    }

    override fun isPlaying(): Boolean {
        if (player != null) {
            return player?.isPlaying()!!
        } else {
            return false
        }
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun isFullScreen(): Boolean {
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            return true
        } else {
            return false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(applicationContext, "Portrait ", Toast.LENGTH_SHORT).show()
        }
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(applicationContext, "Landscape ", Toast.LENGTH_SHORT).show()
        }
    }

    override fun toggleFullScreen() {
        if (isFullScreen()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            recyclerView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.GONE
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        controller?.show()
        return false
    }

    private fun readJsonData() {
        var data = loadJSONFromAsset()
        listData =
            Utility.parseJSON(data)

        adapter.updateVideos(listData, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter=adapter

    }


    fun loadJSONFromAsset(): String? {
        var json: String? = null
        json = try {
            val `is`: InputStream = this.getAssets().open("jsondata.json")
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    class CustomListAdapter(private val videos: MutableList<Video>) :
        RecyclerView.Adapter<CustomListAdapter.CustomViewHolder>() {

        lateinit var context: MainActivity

        fun updateVideos(
            newVideos: List<Video>?,
            mContext: MainActivity
        ) {
            this.context = mContext
            videos.clear()
            videos.addAll(newVideos!!)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.bind(videos[position], position)
        }

        override fun getItemCount(): Int {
            return videos.size
        }

        inner class CustomViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {

            var layout: LinearLayout = itemView.findViewById(R.id.layout)
            var imageView: ImageView = itemView.findViewById(R.id.imageView)
            var title: TextView = itemView.findViewById(R.id.item_title)
            var subtitle: TextView = itemView.findViewById(R.id.item_subtitle)
            fun bind(data: Video?, position: Int) {

                title.text = data?.title
                subtitle.text = data?.subtitle

                Utility.loadImage(
                    imageView,
                    data?.thumb,
                    Utility.getProgressDrawable(imageView.getContext())
                )
                layout.setOnClickListener(View.OnClickListener {
                    Toast.makeText(imageView.context, "Play ", Toast.LENGTH_SHORT).show()
                    context.positionSelected = position
                    context.playVideo(data!!, position)
                })
            }
        }

    }

    fun playVideo(video: Video, position: Int) {
        player?.reset()
        player?.setDataSource(
            this,
            Uri.parse(video.sources[0])
        )
        player?.prepare()
        player?.start()


        val onPreviousClickListener = View.OnClickListener() {
            player?.reset()
            player?.setDataSource(
                this,
                Uri.parse(listData[position - 1].sources[0])
            )
            player?.prepare()
            player?.start()
        }

        val onNextClickListener = View.OnClickListener() {
            player?.reset()
            player?.setDataSource(
                this,
                Uri.parse(listData[position + 1].sources[0])
            )
            player?.prepare()
            player?.start()
        }


        when (position) {
            listData.size - 1 -> {
                controller?.setPrevNextListeners(null, onPreviousClickListener)
            }
            0 -> {
                controller?.setPrevNextListeners(onNextClickListener, null)
            }
            else -> {
                controller?.setPrevNextListeners(onNextClickListener, onPreviousClickListener)
            }
        }

    }

    fun setDefaultHeight() {
        /*val displayMatrix = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMatrix)
        val height: Int = (35 * displayMatrix.heightPixels) / 100
        videoSurfaceContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height
        )*/
    }
}
