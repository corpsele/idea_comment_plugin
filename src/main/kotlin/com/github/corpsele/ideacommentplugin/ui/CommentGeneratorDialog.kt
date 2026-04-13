package com.github.corpsele.ideacommentplugin.ui

import com.github.corpsele.ideacommentplugin.services.CommentGeneratorService
import com.github.corpsele.ideacommentplugin.services.CommentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui DesignerEditorPanelFacade
import com.intellij.ui.layout.*
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.*

class CommentGeneratorDialog(private val project: Project) : DialogWrapper(project) {

    private val commentTypeList = JBList<CommentType>(CommentType.values().toList())
    private val contentField = JTextArea(5, 40)
    private val authorField = JTextField("Author", 20)
    private val previewArea = JTextArea(8, 40).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }

    private var selectedType = CommentType.SINGLE_LINE

    init {
        title = "Generate Comment Block"
        commentTypeList.selectedValue = CommentType.SINGLE_LINE
        commentTypeList.addListSelectionListener {
            selectedType = commentTypeList.selectedValue
            updatePreview()
        }

        contentField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent) = updatePreview()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent) = updatePreview()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent) = updatePreview()
        })

        authorField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent) = updatePreview()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent) = updatePreview()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent) = updatePreview()
        })

        updatePreview()
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Comment Type:") {
                scrollPane(commentTypeList) {
                    preferredSize = JBUI.size(200, 150)
                }
            }

            row("Content:") {
                scrollPane(contentField) {
                    preferredSize = JBUI.size(400, 100)
                }
            }

            row("Author:") { authorField(grow) }

            row("Preview:") {
                scrollPane(previewArea) {
                    preferredSize = JBUI.size(400, 150)
                }
            }
        }
    }

    private fun updatePreview() {
        val content = contentField.text.trim()
        val author = authorField.text.trim()

        if (content.isNotEmpty()) {
            val comment = CommentGeneratorService.getInstance(project)
                .generateComment(selectedType, content, author)
            previewArea.text = comment
        } else {
            previewArea.text = "// Preview will appear here"
        }
    }

    override fun doValidate(): ValidationInfo? {
        return if (contentField.text.trim().isEmpty()) {
            ValidationInfo("Content cannot be empty", contentField)
        } else {
            null
        }
    }

    fun getGeneratedComment(): String {
        return CommentGeneratorService.getInstance(project)
            .generateComment(selectedType, contentField.text.trim(), authorField.text.trim())
    }

    fun getCommentType(): CommentType {
        return selectedType
    }
}