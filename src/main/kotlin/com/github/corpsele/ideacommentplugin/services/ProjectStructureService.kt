package com.github.corpsele.ideacommentplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import java.util.*

@Service(Service.Level.PROJECT)
class ProjectStructureService(private val project: Project) {

    data class FileNode(
        val name: String,
        val type: String,
        val path: String,
        val children: List<FileNode> = emptyList()
    )

    fun getProjectStructure(rootDir: VirtualFile?): FileNode? {
        if (rootDir == null) return null

        val psiManager = PsiManager.getInstance(project)
        val rootDirectory = psiManager.findDirectory(rootDir) ?: return null

        return buildFileTree(rootDirectory)
    }

    private fun buildFileTree(directory: PsiDirectory): FileNode {
        val children = mutableListOf<FileNode>()

        // 添加子目录
        directory.subdirectories.forEach { subDir ->
            children.add(buildFileTree(subDir))
        }

        // 添加文件
        directory.files.forEach { file ->
            children.add(FileNode(
                name = file.name,
                type = getFileType(file),
                path = file.virtualFile.path,
                children = emptyList()
            ))
        }

        return FileNode(
            name = directory.name,
            type = "DIRECTORY",
            path = directory.virtualFile.path,
            children = children.sortedBy { it.name }
        )
    }

    private fun getFileType(file: PsiFile): String {
        return when (file.fileType.name) {
            "JAVA" -> "Java Class"
            "KOTLIN" -> "Kotlin Class"
            "XML" -> "XML File"
            "PROPERTIES" -> "Properties File"
            "JSON" -> "JSON File"
            "YAML" -> "YAML File"
            "MARKDOWN" -> "Markdown File"
            else -> file.fileType.name
        }
    }

    fun getProjectStatistics(rootDir: VirtualFile?): Map<String, Any> {
        if (rootDir == null) return emptyMap()

        val stats = mutableMapOf<String, Any>()
        var totalFiles = 0
        val fileTypeCounts = mutableMapOf<String, Int>()
        var totalLines = 0
        var totalDirectories = 0

        traverseDirectory(rootDir, fileTypeCounts, { totalFiles++; totalDirectories++ }) { file ->
            totalFiles++
            val fileType = file.fileType.name
            fileTypeCounts[fileType] = fileTypeCounts.getOrDefault(fileType, 0) + 1

            // 计算行数
            val content = file.contentsToByteArray().toString(Charsets.UTF_8)
            totalLines += content.lines().size
        }

        stats["totalFiles"] = totalFiles
        stats["totalDirectories"] = totalDirectories
        stats["totalLines"] = totalLines
        stats["fileTypeDistribution"] = fileTypeCounts

        return stats
    }

    private fun traverseDirectory(
        dir: VirtualFile,
        fileTypeCounts: MutableMap<String, Int>,
        onDirectory: (VirtualFile) -> Unit,
        onFile: (VirtualFile) -> Unit
    ) {
        onDirectory(dir)

        for (child in dir.children) {
            if (child.isDirectory) {
                traverseDirectory(child, fileTypeCounts, onDirectory, onFile)
            } else {
                onFile(child)
            }
        }
    }

    companion object {
        fun getInstance(project: Project): ProjectStructureService {
            return project.service<ProjectStructureService>()
        }
    }
}