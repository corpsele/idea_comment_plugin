package com.github.corpsele.ideacommentplugin.actions

import com.github.corpsele.ideacommentplugin.services.CommentGeneratorService
import com.github.corpsele.ideacommentplugin.ui.CommentGeneratorDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class GenerateCommentAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        val dialog = CommentGeneratorDialog(project)
        if (dialog.showAndGet()) {
            val comment = dialog.getGeneratedComment()
            CommentGeneratorService.getInstance(project).insertCommentAtCaret(file, comment)
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.PSI_FILE)

        e.presentation.isEnabled = project != null && editor != null && file != null
    }
}