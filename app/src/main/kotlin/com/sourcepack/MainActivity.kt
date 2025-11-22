package com.sourcepack

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sourcepack.core.Str
import com.sourcepack.ui.*
import com.sourcepack.ui.theme.AppTheme
import com.sourcepack.viewmodel.MainVM
import com.sourcepack.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            val vm: MainVM by viewModels() 
            val theme by vm.theme.collectAsStateWithLifecycle() 
            AppTheme(appTheme = theme) { AppContent(vm, this) }
        }
    }
    
    fun share(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, Str.get("分享", "Share")))
        } catch (_: Exception) {}
    }
}

@Composable
fun AppContent(vm: MainVM, act: MainActivity) {
    var page by remember { mutableStateOf(Page.HOME) }
    
    BackHandler(page != Page.HOME) {
        if(page == Page.CONFIG_GEN || page == Page.CONFIG_BL) page = Page.CONFIG_ROOT else page = Page.HOME
    }
    
    AnimatedContent(targetState = page, label = "Nav") { p ->
        when (p) {
            Page.HOME -> HomeScreen(vm, { page = Page.CONFIG_ROOT }, { act.share(it) })
            Page.CONFIG_ROOT -> SettingsRoot(onBack = { page = Page.HOME }, onNav = { page = it }, vm = vm)
            Page.CONFIG_GEN -> GeneralSettings(vm, { page = Page.CONFIG_ROOT })
            Page.CONFIG_BL -> BlacklistSettings(vm, { page = Page.CONFIG_ROOT })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: MainVM, toCfg: () -> Unit, onShare: (Uri) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val cfg by vm.cfg.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var sourceUris by remember { mutableStateOf<List<Uri>?>(null) }
    var gitUrl by remember { mutableStateOf<String?>(null) }
    var projectName by remember { mutableStateOf("Project") }
    var mode by remember { mutableIntStateOf(0) } // 0=Dir, 1=Files, 2=Git

    val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { destUri ->
        destUri?.let { dest ->
            when (mode) {
                0 -> sourceUri?.let { vm.packDirectly(it, dest) }
                1 -> sourceUris?.let { vm.packListDirectly(it, dest) }
                2 -> gitUrl?.let { vm.runGit(it, dest) }
            }
        }
    }

    fun launchSaver() {
        val ext = if(cfg.format == com.sourcepack.data.Format.XML) "xml" else "md"
        val prefix = if (mode == 2) {
             val clean = gitUrl?.trim()?.removeSuffix("/")?.removeSuffix(".git") ?: "GitHub"
             clean.substringAfterLast("/")
        } else {
             projectName
        }
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        saver.launch("${prefix}_$time.$ext")
    }

    val launcherDir = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { 
            sourceUri = it
            val doc = DocumentFile.fromTreeUri(context, it)
            projectName = doc?.name ?: "Project"
            mode = 0
            launchSaver() 
        } 
    }

    val launcherFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { 
        if(it.isNotEmpty()) { 
            sourceUris = it
            projectName = "Selected_Files"
            mode = 1
            launchSaver() 
        }
    }
    
    var showGitDialog by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = {}, actions = { IconButton(onClick = toCfg) { Icon(Ico.Settings, null) } }) }) { pad ->
        Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val s = state) {
                UiState.Idle -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(Ico.Inventory2, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("SourcePack", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(48.dp))
                    HomeBtn(Ico.Folder, Str.get("选择文件夹", "Project Folder"), Str.get("扫描并导出", "Scan & Export")) { launcherDir.launch(null) }
                    Spacer(Modifier.height(16.dp))
                    HomeBtn(Ico.Description, Str.get("选择文件", "Select Files"), Str.get("挑选文件并导出", "Pick & Export")) { launcherFiles.launch(arrayOf("*/*")) }
                    Spacer(Modifier.height(16.dp))
                    HomeBtn(Ico.CloudDownload, "GitHub", Str.get("下载并打包", "Download & Pack")) { showGitDialog = true }
                }
                
                is UiState.Loading -> LoadingView(msg = s.msg, detail = s.detail)

                is UiState.Success -> ResultCard(
                    success = true, 
                    msg = s.info, 
                    onShare = { s.uri?.let { onShare(it) } }, 
                    onReset = { vm.reset() }
                )
                
                // 修改：传入错误日志 URI，如果有的话，按钮会变成红色并分享日志
                is UiState.Error -> ResultCard(
                    success = false, 
                    msg = s.err, 
                    onShare = if (s.logUri != null) { { onShare(s.logUri) } } else null, 
                    onReset = { vm.reset() },
                    errorLogUri = s.logUri
                )
                
                else -> {}
            }
        }
    }
    
    if (showGitDialog) {
        var url by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showGitDialog = false },
            title = { Text("GitHub URL") },
            text = { 
                Column {
                    Text(Str.get("输入仓库地址。我们将下载并构建完整的文件树结构。", "Enter Repo URL. We will download and build the full tree structure."))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(url, { url = it }, placeholder = { Text("https://github.com/user/repo") }, modifier = Modifier.fillMaxWidth()) 
                }
            },
            confirmButton = { 
                Button(onClick = { 
                    if(url.length > 10) { 
                        gitUrl = url
                        mode = 2
                        showGitDialog = false
                        launchSaver()
                    } 
                }) { Text(Str.get("下一步", "Next")) } 
            }
        )
    }
}