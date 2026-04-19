package `in`.aibobscstewa.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var progressBar: View
    private lateinit var offlineView: View

    private val homeUrl = "https://aibobscstewa.com/"
    private val allowedHost = "aibobscstewa.com"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        webView = findViewById(R.id.web_view)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        progressBar = findViewById(R.id.progress_bar)
        offlineView = findViewById(R.id.offline_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        setupWebView()
        setupSwipeRefresh()
        setupBackNav()

        findViewById<View>(R.id.btn_retry).setOnClickListener {
            if (isOnline()) {
                offlineView.visibility = View.GONE
                webView.visibility = View.VISIBLE
                webView.loadUrl(homeUrl)
            } else {
                Toast.makeText(this, R.string.still_offline, Toast.LENGTH_SHORT).show()
            }
        }

        if (isOnline()) {
            webView.loadUrl(homeUrl)
        } else {
            showOffline()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = "$userAgentString AIBOBSCSTEWAApp/1.0"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                val host = request.url.host ?: ""

                if (!url.startsWith("http")) {
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, request.url))
                        true
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, R.string.cant_open_link, Toast.LENGTH_SHORT).show()
                        true
                    }
                }

                return if (host.contains(allowedHost)) {
                    false
                } else {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, request.url))
                    } catch (_: Exception) {
                        view.loadUrl(url)
                    }
                    true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true && !isOnline()) {
                    showOffline()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!title.isNullOrBlank() && !title.startsWith("http")) {
                    supportActionBar?.title = title
                }
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimetype)
                request.addRequestHeader("User-Agent", userAgent)
                val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                request.setTitle(fileName)
                request.setDescription(getString(R.string.downloading))
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this, getString(R.string.download_started, fileName), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.aibob_orange, R.color.aibob_blue)
        swipeRefresh.setOnRefreshListener {
            if (isOnline()) {
                webView.reload()
            } else {
                swipeRefresh.isRefreshing = false
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackNav() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    webView.canGoBack() -> webView.goBack()
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }
    }

    private fun showOffline() {
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        offlineView.visibility = View.VISIBLE
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val url = when (item.itemId) {
            R.id.nav_home -> homeUrl
            R.id.nav_about -> "https://aibobscstewa.com/about/"
            R.id.nav_objectives -> "https://aibobscstewa.com/objectives/"
            R.id.nav_committee -> "https://aibobscstewa.com/committee/"
            R.id.nav_office_bearers -> "https://aibobscstewa.com/office-bearers/"
            R.id.nav_circulars -> "https://aibobscstewa.com/circulars/"
            R.id.nav_notices -> "https://aibobscstewa.com/notices/"
            R.id.nav_news -> "https://aibobscstewa.com/news/"
            R.id.nav_events -> "https://aibobscstewa.com/events/"
            R.id.nav_gallery -> "https://aibobscstewa.com/gallery/"
            R.id.nav_downloads -> "https://aibobscstewa.com/downloads/"
            R.id.nav_reservation -> "https://aibobscstewa.com/reservation-policy/"
            R.id.nav_contact -> "https://aibobscstewa.com/contact/"
            R.id.nav_share -> {
                shareApp()
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_about_app -> {
                startActivity(Intent(this, AboutActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            else -> homeUrl
        }

        if (isOnline()) {
            offlineView.visibility = View.GONE
            webView.visibility = View.VISIBLE
            webView.loadUrl(url)
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
