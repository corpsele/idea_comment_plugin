package com.github.corpsele.ideacommentplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class CommentType {
    SINGLE_LINE,
    MULTI_LINE,
    JAVADOC,
    BLOCK,
    HEADER,
    LICENSE
}

@Service(Service.Level.PROJECT)
class CommentGeneratorService(private val project: Project) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun generateComment(commentType: CommentType, content: String, author: String = "Author"): String {
        return when (commentType) {
            CommentType.SINGLE_LINE -> "// $content"
            CommentType.MULTI_LINE -> "/*\n * $content\n */"
            CommentType.JAVADOC -> "/**\n * $content\n * @author $author\n * @date ${LocalDateTime.now().format(dateFormatter)}\n */"
            CommentType.BLOCK -> "/*\n${content.split("\n").joinToString("\n") { " * $it" }}\n */"
            CommentType.HEADER -> """
                /**************************************************************
                 * $content
                 * 
                 * Author: $author
                 * Created: ${LocalDateTime.now().format(dateFormatter)}
                 * Description: Auto-generated comment block
                 **************************************************************/
                """.trimIndent()
            CommentType.LICENSE -> """
                /*
                 * Copyright ${LocalDateTime.now().year} $author
                 * 
                 * Licensed under the Apache License, Version 2.0 (the "License");
                 * you may not use this file except in compliance with the License.
                 * You may obtain a copy of the License at
                 * 
                 *     http://www.apache.org/licenses/LICENSE-2.0
                 * 
                 * Unless required by applicable law or agreed to in writing, software
                 * distributed under the License is distributed on an "AS IS" BASIS,
                 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                 * See the License for the specific language governing permissions and
                 * limitations under the License.
                 */
                """.trimIndent()
        }
    }

    fun insertCommentAtCaret(file: PsiFile, comment: String) {
        val elementFactory = PsiElementFactory.getInstance(project)
        val codeStyleManager = CodeStyleManager.getInstance(project)

        val commentElement = elementFactory.createCommentFromText(comment, null)
        val caretModel = file.viewProvider.document?.let {
            val editor = com.intellij.openapi.editor.EditorFactory.getInstance().getEditors(it, project).firstOrNull()
            editor?.caretModel
        }

        if (caretModel != null && caretModel.offset < file.textLength) {
            val elementAtCaret = file.findElementAt(caretModel.offset)
            if (elementAtCaret != null) {
                val parent = elementAtCaret.parent
                val newComment = parent.addBefore(commentElement, elementAtCaret)
                codeStyleManager.reformat(newComment)
            }
        }
    }

    companion object {
        fun getInstance(project: Project): CommentGeneratorService {
            return project.service<CommentGeneratorService>()
        }
    }
}