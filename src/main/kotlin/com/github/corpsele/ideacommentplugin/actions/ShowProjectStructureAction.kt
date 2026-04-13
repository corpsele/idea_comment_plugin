package com.github.corpsele.ideacommentplugin.actions

import com.github.corpsele.ideacommentplugin.services.ProjectStructureService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class ShowProjectStructureAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val structureService = ProjectStructureService.getInstance(project)
        val projectDir = project.baseDir ?: return

        val root = structureService.getProjectStructure(projectDir) ?: return

        // 构建树模型
        val rootNode = convertToTreeNode(root)
        val treeModel = DefaultTreeModel(rootNode)

        // 创建树形组件
        val tree = javax.swing.JTree(treeModel)
        tree.isRootVisible = true
        tree.showsRootHandles = true

        // 展开根节点
        val rootPath = TreePath(rootNode.path)
        tree.expandPath(rootPath)

        val scrollPane = javax.swing.JScrollPane(tree)
        scrollPane.preferredSize = java.awt.Dimension(600, 400)

        Messages.showInfoMessage(
            project,
            scrollPane,
            "Project Structure - ${project.name}"
        )
    }

    private fun convertToTreeNode(node: ProjectStructureService.FileNode): DefaultMutableTreeNode {
        val treeNode = DefaultMutableTreeNode(node.name)

        node.children.forEach { child ->
            treeNode.add(convertToTreeNode(child))
        }

        return treeNode
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}