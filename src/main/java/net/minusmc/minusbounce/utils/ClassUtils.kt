package net.minusmc.minusbounce.utils

import net.minecraftforge.fml.common.Loader
import net.minusmc.minusbounce.plugin.Plugin
import net.minusmc.minusbounce.value.Value
import org.apache.logging.log4j.core.config.plugins.ResolverUtil
import net.minecraft.launchwrapper.IClassTransformer
import java.lang.reflect.Modifier
import java.io.File
import java.util.jar.JarFile

object ClassUtils {

    private val cachedClasses = mutableMapOf<String, Boolean>()
    private val classCache = mutableMapOf<String, MutableList<String>>()
    val killSultFiles = mutableListOf<String>()
    val capeFiles = mutableListOf<String>()

    fun initCacheClass() {
        val files = Loader.instance().discoverer.getNonModLibs()
        val jarFiles = files.filter { it.extension.equals("jar", true) }
        jarFiles.forEach {
            val jarFile = JarFile(it)
            classCache[it.name] = mutableListOf()
            for (entry in jarFile.entries()) {
                if (entry.name.endsWith(".class")) {
                    val className = entry.name.removeSuffix(".class").replace("/", ".")
                    classCache[it.name]!!.add(className)
                } else if (entry.name.endsWith(".txt") && entry.name.contains("killsults")) {
                    killSultFiles.add("/" + entry.name)
                } else if (entry.name.endsWith(".png") && entry.name.contains("cape")) {
                    capeFiles.add(entry.name.removePrefix("assets/minecraft/"))
                }
            }
        }
    }

    private fun hasClass(className: String): Boolean {
        return if (cachedClasses.containsKey(className)) {
            cachedClasses[className]!!
        } else try {
            Class.forName(className)
            cachedClasses[className] = true

            true
        } catch (e: ClassNotFoundException) {
            cachedClasses[className] = false

            false
        }
    }

    fun getObjectInstance(clazz: Class<*>): Any {
        clazz.declaredFields.forEach {
            if (it.name.equals("INSTANCE")) {
                return it.get(null)
            }
        }
        throw IllegalAccessException("This class not a kotlin object")
    }

    fun getValues(clazz: Class<*>, instance: Any) = clazz.declaredFields.map { valueField ->
        valueField.isAccessible = true
        valueField[instance]
    }.filterIsInstance<Value<*>>()


    fun <T : Any> resolvePackage(packagePath: String, klass: Class<T>): List<Class<out T>> {

        val resolver = ResolverUtil()

        resolver.classLoader = klass.classLoader

        resolver.findInPackage(object : ResolverUtil.ClassTest() {
            override fun matches(type: Class<*>): Boolean {
                return true
            }
        }, packagePath)

        val list = mutableListOf<Class<out T>>()

        for(resolved in resolver.classes) {
            resolved.declaredMethods.find {
                Modifier.isNative(it.modifiers)
            }?.let {
                val klass1 = it.declaringClass.typeName+"."+it.name
                throw UnsatisfiedLinkError(klass1+"\n\tat ${klass1}(Native Method)") // we don't want native methods
            }
            if(klass.isAssignableFrom(resolved) && !resolved.isInterface && !Modifier.isAbstract(resolved.modifiers))
                list.add(resolved as Class<out T>)
        }

        return list
    }


    private fun isClassSkip1(clazzName: String): Boolean {
        return (clazzName.equals("net.minusmc.minusbounce.MinusBounce", true) || 
                clazzName.contains("net.minusmc.minusbounce.injection")|| 
                clazzName.contains("optifine")) // optifine
    }

    private fun isClassSkip2(clazzName: String): Boolean {
        return (clazzName.startsWith("kotlin") || clazzName.startsWith("org.jetbrains") || clazzName.startsWith("org.intellij") // kotlin
             || clazzName.startsWith("jdk.nashorn") // script plugin
             || clazzName.startsWith("org.newsclub.net") // discord rpc plugin
             || clazzName.startsWith("com.viaversion")) // via version plugin
    }

    fun resolvePlugins(): List<Class<out Plugin>> {
        val pluginClass = mutableListOf<Class<out Plugin>>()
        classCache.values.forEach listClasses@ {
            jarClasses -> jarClasses.forEach checkClass@{
                if (isClassSkip1(it)) return@listClasses
                if (isClassSkip2(it)) return@checkClass
                try {
                    val clazz = Class.forName(it)
                    if (Plugin::class.java.isAssignableFrom(clazz)) pluginClass.add(clazz as Class<out Plugin>)
                } catch (e: Exception) {
                    ClientUtils.logger.error("Error while loading class $it: $e")
                }
            }
        }
        return pluginClass
    }
    fun hasForge() = hasClass("net.minecraftforge.common.MinecraftForge")
}