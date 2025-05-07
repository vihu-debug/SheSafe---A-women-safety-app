package com.shesafe

import android.content.pm.PackageManager
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.Manifest
import android.widget.LinearLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat


@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val url = "https://shivangi-0123.github.io/shesafe"
    val context = LocalContext.current

    // Permission state
    val hasLocationPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasLocationPermission.value = fineLocationGranted || coarseLocationGranted
    }

    // Request permissions when composable is first launched
    LaunchedEffect(Unit) {
        if (!hasLocationPermission.value) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Loading indicator state
        val isLoading = remember { mutableStateOf(true) }
        // Track if WebView can go back
        val canGoBack = rememberSaveable { mutableStateOf(false) }
        // Store WebView reference
        val webView = remember { mutableStateOf<WebView?>(null) }

        // Handle back button press
        BackHandler(enabled = canGoBack.value) {
            webView.value?.goBack()
        }

        // Use AndroidView to embed a WebView
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false

                        // Enable location settings
                        setGeolocationEnabled(true)
                        javaScriptCanOpenWindowsAutomatically = true
                    }

                    webViewClient = object : android.webkit.WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading.value = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading.value = false
                            canGoBack.value = this@apply.canGoBack()
                        }

                        // Handle URLs properly, allowing external intents when appropriate
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            if (url == null) return false

                            // Handle standard web URLs within the WebView
                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                view?.loadUrl(url)
                                isLoading.value = true
                                return true
                            }

                            // Handle other schemes (tel:, mailto:, whatsapp:, etc.) with external apps
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                                return true
                            } catch (e: Exception) {
                                return false
                            }
                        }
                    }

                    // Add WebChromeClient to handle location permission requests
                    webChromeClient = object : WebChromeClient() {
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?
                        ) {
                            callback?.invoke(origin, hasLocationPermission.value, false)
                        }
                    }

                    setOnKeyListener { _, keyCode, event ->
                        if (event.action == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                            if (canGoBack()) {
                                goBack()
                                return@setOnKeyListener true
                            }
                        }
                        false
                    }

                    loadUrl(url)
                    webView.value = this
                }
            },
            update = { view ->
                // Update WebView state when recomposing
                webView.value = view
                canGoBack.value = view.canGoBack()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Show loading indicator when page is loading
        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}