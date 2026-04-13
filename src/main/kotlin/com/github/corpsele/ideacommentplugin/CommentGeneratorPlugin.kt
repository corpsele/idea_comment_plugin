package com.github.corpsele.ideacommentplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class CommentGeneratorPlugin : StartupActivity {
    override fun runActivity(project: Project) {
        // 初始化插件
        println("Comment Generator Plugin initialized")
    }
}