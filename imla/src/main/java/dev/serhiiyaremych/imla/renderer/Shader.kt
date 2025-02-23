/*
 * Copyright 2024, Serhii Yaremych
 * SPDX-License-Identifier: MIT
 */

@file:Suppress("unused")

package dev.serhiiyaremych.imla.renderer

import android.content.res.AssetManager
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import dev.romainguy.kotlin.math.Mat3
import dev.romainguy.kotlin.math.Mat4
import dev.serhiiyaremych.imla.renderer.opengl.OpenGLShader
import org.intellij.lang.annotations.Language
import java.io.InputStream

internal interface Shader {
    val name: String
    fun bind()
    fun unbind()

    fun bindUniformBlock(blockName: String, bindingPoint: Int)

    fun setInt(name: String, value: Int)
    fun setIntArray(name: String, vararg values: Int)
    fun setFloatArray(name: String, vararg values: Float)
    fun setFloat(name: String, value: Float)
    fun setFloat2(name: String, value: Float2)
    fun setFloat3(name: String, value: Float3)
    fun setFloat4(name: String, value: Float4)
    fun setMat3(name: String, value: Mat3)
    fun setMat4(name: String, value: Mat4)

    fun uploadUniformInt(name: String, value: Int)
    fun uploadUniformIntArray(name: String, vararg values: Int)

    fun uploadFloatArray(name: String, vararg values: Float)
    fun uploadUniformFloat(name: String, value: Float)
    fun uploadUniformFloat2(name: String, value: Float2)
    fun uploadUniformFloat3(name: String, value: Float3)
    fun uploadUniformFloat4(name: String, value: Float4)

    fun uploadUniformMat3(name: String, value: Mat3)
    fun uploadUniformMat4(name: String, value: Mat4)

    fun destroy()

    companion object {
        private const val TAG = "Shader"

        private fun dropExtension(fileName: String): String {
            val lastIndexOfDot = fileName.lastIndexOf(".")
            return if (lastIndexOfDot != -1) fileName.substring(0, lastIndexOfDot) else fileName
        }

        private fun readWithCloseStream(inputStream: InputStream): String {
            return inputStream.bufferedReader().readText().also { inputStream.close() }
        }

        fun create(assetManager: AssetManager, vertexAsset: String, fragmentAsset: String): Shader {
            return OpenGLShader(
                name = dropExtension(vertexAsset),
                vertexSrc = readWithCloseStream(assetManager.open(vertexAsset)),
                fragmentSrc = readWithCloseStream(assetManager.open(fragmentAsset))
            )
        }

        fun create(
            name: String,
            @Language("GLSL") vertexSrc: String,
            @Language("GLSL") fragmentSrc: String
        ): Shader {
            return OpenGLShader(name, vertexSrc, fragmentSrc)
        }
    }
}

internal class ShaderLibrary {
    private val shaders: MutableMap<String, Shader> = mutableMapOf()

    fun load(assetManager: AssetManager, vertexAsset: String, fragmentAsset: String): Shader {
        val shader = Shader.create(assetManager, vertexAsset, fragmentAsset)
        add(shader, shader.name)
        return shader
    }

    fun add(shader: Shader, name: String = "") {
        val shaderName = name.takeIf { it.isNotEmpty() } ?: shader.name
        require(shaders[shaderName] == null) { "Shader $shaderName already exists!" }
        shaders[shaderName] = shader
    }

    operator fun get(name: String): Shader {
        return requireNotNull(shaders[name]) { "Shader $name not found!" }
    }

    fun destroyAll() {
        shaders.forEach { (_, shader) ->
            shader.destroy()
        }
        shaders.clear()
    }
}