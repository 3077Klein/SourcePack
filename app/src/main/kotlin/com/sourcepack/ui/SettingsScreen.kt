package com.sourcepack.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sourcepack.core.Str
import com.sourcepack.data.*
import com.sourcepack.viewmodel.MainVM
import com.sourcepack.Page
import com.sourcepack.BuildConfig 
/**
 * 设置主页面
 * 包含常规配置、黑名单入口、外观设置、关于信息及开源声明
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoot(onBack: () -> Unit, onNav: (Page) -> Unit, vm: MainVM) {
    val currentTheme by vm.theme.collectAsState()
    var showLicense by remember { mutableStateOf(false) } // 项目本身的许可
    var showLibs by remember { mutableStateOf(false) }    // 第三方库声明
    val ctx = LocalContext.current

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text(Str.get("设置", "Settings")) }, 
                navigationIcon = { IconButton(onClick = onBack) { Icon(Ico.ArrowBack, null) } }
            ) 
        }
    ) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState())) {
            
            // --- 常规 ---
            SettingHeader(Str.get("常规", "General"))
            SettingLink(Ico.Settings, Str.get("通用配置", "General Config"), Str.get("格式、压缩、模式", "Compress, Format, Mode")) { onNav(Page.CONFIG_GEN) }
            SettingLink(Ico.Delete, Str.get("黑名单管理", "Blacklist"), Str.get("管理忽略规则 (批量)", "Manage Ignored Files")) { onNav(Page.CONFIG_BL) }
            
            HorizontalDivider(Modifier.padding(vertical = 8.dp).alpha(0.5f))
            
            // --- 外观 ---
            SettingHeader(Str.get("外观", "Appearance"))
            
            // 主题选择对话框逻辑
            var showThemeDialog by remember { mutableStateOf(false) }
            ListItem(
                modifier = Modifier.clickable { showThemeDialog = true },
                headlineContent = { Text(Str.get("主题", "Theme")) },
                supportingContent = { Text(when(currentTheme) {
                    AppTheme.SYSTEM -> Str.get("跟随系统", "System")
                    AppTheme.BLUE -> Str.get("经典蓝", "Classic Blue")
                    AppTheme.PURPLE -> Str.get("默认紫", "Default Purple")
                    AppTheme.GRAY -> Str.get("极简灰", "Minimal Gray")
                }) },
                leadingContent = { Icon(Ico.Palette, null) }
            )
            
            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = { Text(Str.get("选择主题", "Select Theme")) },
                    text = {
                        Column {
                            AppTheme.values().forEach { t ->
                                Row(
                                    Modifier.fillMaxWidth().clickable { vm.setTheme(t); showThemeDialog = false }.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(t.name)
                                    if (currentTheme == t) Icon(Ico.Check, null)
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text(Str.get("取消", "Cancel")) } }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp).alpha(0.5f))
            
            // --- 关于 ---
            SettingHeader(Str.get("关于", "About"))
            
            ListItem(
                headlineContent = { Text("SourcePack") },
                supportingContent = { Text("v${BuildConfig.VERSION_NAME}\n${Str.get("构建变体", "Variant")}: Release") },
                leadingContent = { Icon(Ico.Info, null) }
            )
            
            ListItem(
                modifier = Modifier.clickable { 
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${Str.AUTHOR_EMAIL}")
                    }
                    try { ctx.startActivity(intent) } catch(_:Exception){}
                },
                headlineContent = { Text(Str.get("作者", "Author")) },
                supportingContent = { Text("${Str.AUTHOR_NAME} (AI: Gemini-3.0-Pro)\n${Str.AUTHOR_EMAIL}") },
                leadingContent = { Icon(Ico.Folder, null) }
            )
            
            // 1. 项目许可 (SourcePack 自己的 License)
            ListItem(
                modifier = Modifier.clickable { showLicense = true },
                headlineContent = { Text(Str.get("项目许可", "Project License")) },
                supportingContent = { Text("Apache License 2.0") },
                leadingContent = { Icon(Ico.Copyright, null) }
            )
            
            // 2. 第三方库声明 (对他人的致谢)
            ListItem(
                modifier = Modifier.clickable { showLibs = true },
                headlineContent = { Text(Str.get("第三方开源库", "Open Source Libraries")) },
                supportingContent = { Text(Str.get("致谢 Kotlin, AndroidX, OkHttp...", "Credits to Kotlin, AndroidX...")) },
                leadingContent = { Icon(Ico.Description, null) }
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
    
    // 项目许可弹窗
    if (showLicense) {
        AlertDialog(
            onDismissRequest = { showLicense = false },
            title = { Text("SourcePack License") },
            text = { 
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(Str.LICENSE_TEXT, style = MaterialTheme.typography.bodySmall) 
                }
            },
            confirmButton = { Button(onClick = { showLicense = false }) { Text("OK") } }
        )
    }
    
    // 第三方库弹窗
    if (showLibs) {
        AlertDialog(
            onDismissRequest = { showLibs = false },
            title = { Text(Str.get("第三方库", "Open Source Libraries")) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(Str.get("本项目使用了以下开源库 (点击跳转)：", "This project uses the following open source libraries (Tap to open):"), style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(16.dp))
                    
                    // 在这里填入了真实的 GitHub 或官方地址
                    LibraryItem("Kotlin", "JetBrains", "Apache 2.0", "https://github.com/JetBrains/kotlin")
                    LibraryItem("Jetpack Compose", "Google", "Apache 2.0", "https://androidx.tech")
                    LibraryItem("Material Design 3", "Google", "Apache 2.0", "https://github.com/material-components/material-components-android")
                    LibraryItem("AndroidX", "Google", "Apache 2.0", "https://github.com/androidx/androidx")
                    LibraryItem("OkHttp", "Square, Inc.", "Apache 2.0", "https://github.com/square/okhttp")
                    LibraryItem("DocumentFile", "Google", "Apache 2.0", "https://developer.android.com/reference/androidx/documentfile/provider/DocumentFile")
                }
            },
            confirmButton = { TextButton(onClick = { showLibs = false }) { Text("OK") } }
        )
    }
}

/**
 * 通用配置页面
 * 管理过滤规则开关、输出格式和模式
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeneralSettings(vm: MainVM, back: () -> Unit) {
    val cfg by vm.cfg.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text(Str.get("通用配置", "General")) }, navigationIcon = { IconButton(onClick = back) { Icon(Ico.ArrowBack, null) } }) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState())) {
            SettingHeader(Str.get("过滤", "Filtering"))
            SwitchItem(Str.get("使用 .gitignore", "Use .gitignore"), cfg.useGitIgnore) { vm.saveCfg(cfg.copy(useGitIgnore = it)) }
            SwitchItem(Str.get("忽略 .gradle 文件夹", "Ignore .gradle dir"), cfg.ignoreGradle) { vm.saveCfg(cfg.copy(ignoreGradle = it)) }
            SwitchItem(Str.get("忽略 .git 文件夹", "Ignore .git dir"), cfg.ignoreGit) { vm.saveCfg(cfg.copy(ignoreGit = it)) }
            SwitchItem(Str.get("忽略 build 文件夹", "Ignore build dir"), cfg.ignoreBuild) { vm.saveCfg(cfg.copy(ignoreBuild = it)) }
            
            SettingHeader(Str.get("输出内容", "Output Content"))
            SwitchItem(Str.get("压缩内容 (去换行)", "Compress content"), cfg.compress) { vm.saveCfg(cfg.copy(compress = it)) }
            
            SettingHeader(Str.get("输出格式", "Output Format"))
            FlowRow(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Format.values().forEach { f ->
                    FilterChip(
                        selected = cfg.format == f,
                        onClick = { vm.saveCfg(cfg.copy(format = f)) },
                        label = { Text(f.name) }
                    )
                }
            }

            SettingHeader(Str.get("输出模式", "Output Mode"))
            FlowRow(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mode.values().forEach { m ->
                    FilterChip(
                        selected = cfg.mode == m,
                        onClick = { vm.saveCfg(cfg.copy(mode = m)) },
                        label = { 
                            Text(when(m) {
                                Mode.FULL -> Str.get("完整内容", "Full Content")
                                Mode.TREE -> Str.get("仅目录树", "Tree Only")
                            })
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * 黑名单管理页面
 * 支持按文件名或后缀名管理过滤规则
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlacklistSettings(vm: MainVM, back: () -> Unit) {
    val files by vm.uFiles.collectAsState()
    val exts by vm.uExts.collectAsState()
    var type by remember { mutableIntStateOf(0) } // 0: 文件名, 1: 后缀
    val selectedItems = remember { mutableStateListOf<String>() }
    LaunchedEffect(type) { selectedItems.clear() }

    val currentList = (if(type==0) files else exts).toList().sorted()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    if (selectedItems.isEmpty()) Text(Str.get("黑名单", "Blacklist")) 
                    else Text("${selectedItems.size} ${Str.get("已选择", "Selected")}")
                },
                navigationIcon = { 
                    if (selectedItems.isEmpty()) IconButton(onClick = back) { Icon(Ico.ArrowBack, null) }
                    else IconButton(onClick = { selectedItems.clear() }) { Icon(Ico.UnselectAll, null) }
                },
                actions = {
                    if (selectedItems.isNotEmpty()) {
                        IconButton(onClick = {
                            vm.removeBlacklist(type, selectedItems.toList())
                            selectedItems.clear()
                        }) { Icon(Ico.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    } else {
                        IconButton(onClick = { 
                            selectedItems.clear()
                            selectedItems.addAll(currentList)
                        }) { Icon(Ico.SelectAll, null) }
                    }
                }
            ) 
        },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Ico.Add, null) } }
    ) { p ->
        Column(Modifier.padding(p)) {
            TabRow(selectedTabIndex = type) {
                Tab(selected = type==0, onClick = { type=0 }, text = { Text(Str.get("文件名/文件夹","Names")) })
                Tab(selected = type==1, onClick = { type=1 }, text = { Text(Str.get("后缀 (.ext)","Extensions")) })
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(currentList) { item ->
                    val isSel = item in selectedItems
                    ListItem(
                        modifier = Modifier.clickable { 
                            if(isSel) selectedItems.remove(item) else selectedItems.add(item) 
                        },
                        leadingContent = { Checkbox(checked = isSel, onCheckedChange = { if(it) selectedItems.add(item) else selectedItems.remove(item) }) },
                        headlineContent = { Text(item) }
                    )
                }
                if (currentList.isEmpty()) item { Text(Str.get("空列表", "Empty"), Modifier.padding(32.dp), color = Color.Gray) }
            }
        }
    }
    
    if (showAdd) {
        var txt by remember { mutableStateOf(if(type==1) "." else "") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(Str.get("添加规则","Add Rule")) },
            text = { 
                Column {
                    OutlinedTextField(txt, { txt=it }, singleLine = true, label = { Text(if(type==1) ".ext" else "Name") })
                    if(type==1 && !txt.startsWith(".")) Text(Str.get("必须以 . 开头", "Must start with ."), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { 
                Button(onClick = { 
                    if(txt.isNotEmpty()) { 
                        vm.addBlacklist(type, listOf(txt))
                        showAdd = false 
                    } 
                }) { Text(Str.get("确定", "OK")) } 
            }
        )
    }
}

// --- 更新后的 LibraryItem 组件 ---
@Composable
fun LibraryItem(name: String, author: String, license: String, url: String) {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // 点击时调用系统浏览器打开链接
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    ctx.startActivity(intent)
                } catch (_: Exception) {
                    // 防止没有浏览器导致的崩溃，虽然极少见
                }
            }
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            // 可以在这里加个小图标提示是外链，不过保持简洁也可以
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(license, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(Modifier.padding(top = 8.dp).alpha(0.2f))
    }
}