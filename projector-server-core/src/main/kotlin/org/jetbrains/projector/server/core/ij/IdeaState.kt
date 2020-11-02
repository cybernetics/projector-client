/*
 * MIT License
 *
 * Copyright (c) 2019-2020 JetBrains s.r.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.jetbrains.projector.server.core.ij

import kotlin.concurrent.thread

private fun isIdeaInProperState(ideaClassLoader: ClassLoader?): Boolean {
  val loadingStateClass = Class.forName("com.intellij.diagnostic.LoadingState", false, ideaClassLoader)

  val loadingState = loadingStateClass
    .getDeclaredField("CONFIGURATION_STORE_INITIALIZED")
    .get(null)

  return loadingStateClass
    .getDeclaredMethod("isOccurred")
    .invoke(loadingState) as Boolean
}

public fun invokeWhenIdeaIsInitialized(
  purpose: String,
  onNoIdeaFound: (() -> Unit)? = null,
  onInitialized: (ideaClassLoader: ClassLoader) -> Unit,
) {
  thread(isDaemon = true) {
    if (onNoIdeaFound == null) {
      println("Starting attempts to $purpose")
    }

    while (true) {
      try {
        val ideaMainClassWithIdeaClassLoader = Class.forName("com.intellij.ide.WindowsCommandLineProcessor")
          .getDeclaredField("ourMainRunnerClass")
          .get(null) as Class<*>?

        if (ideaMainClassWithIdeaClassLoader != null) {  // null means we run with IDEA but it's not initialized yet
          val ideaClassLoader = ideaMainClassWithIdeaClassLoader.classLoader

          if (isIdeaInProperState(ideaClassLoader)) {
            onInitialized(ideaClassLoader)

            if (onNoIdeaFound == null) {
              println("\"$purpose\" is done")
            }
            break
          }
        }
      }
      catch (t: Throwable) {
        if (onNoIdeaFound == null) {
          println("Can't $purpose. It's OK if you don't run an IntelliJ platform based app.")
          t.printStackTrace()
        }
        else {
          onNoIdeaFound()
        }
        break
      }

      Thread.sleep(1)
    }
  }
}
