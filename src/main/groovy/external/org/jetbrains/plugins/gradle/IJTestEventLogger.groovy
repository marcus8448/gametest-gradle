/**
 * Copyright 2009-2019 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package external.org.jetbrains.plugins.gradle

import dev.galacticraft.gametest.impl.GameTestRunTask
import groovy.xml.MarkupBuilder
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.testing.*

/**
 * See https://github.com/aSemy/intellij-community/blob/854604b6a346b908794500f7746fa5104d9108d3/plugins/gradle/resources/org/jetbrains/plugins/gradle/IJTestLogger.groovy
 */
class IJTestEventLogger { //TODO: translate groovy to java
  static def configureTestEventLogging(def task) {
    task.addTestListener(
      new TestListener() {
        @Override
        void beforeSuite(TestDescriptor descriptor) {
          logTestEvent("beforeSuite", descriptor, null, null)
        }

        @Override
        void afterSuite(TestDescriptor descriptor, TestResult result) {
          logTestEvent("afterSuite", descriptor, null, result)
        }

        @Override
        void beforeTest(TestDescriptor descriptor) {
          logTestEvent("beforeTest", descriptor, null, null)
        }

        @Override
        void afterTest(TestDescriptor descriptor, TestResult result) {
          logTestEvent("afterTest", descriptor, null, result)
        }
      }
    )

    task.addTestOutputListener(new TestOutputListener() {
      @Override
      void onOutput(TestDescriptor descriptor, TestOutputEvent event) {
        logTestEvent("onOutput", descriptor, event, null)
      }
    })
  }

  static def logTestEvent(testEventType, TestDescriptor testDescriptor, testEvent, testResult) {
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.event(type: testEventType) {
      test(id: testDescriptor.id, parentId: testDescriptor.parent?.id ?: '') {
        if (testDescriptor) {
          descriptor(name: testDescriptor.name ?: '', displayName: getName(testDescriptor) ?: '', className: testDescriptor.className ?: '')
        }
        if (testEvent) {
          def message = escapeCdata(testEvent.message)
          event(destination: testEvent.destination) {
            xml.mkp.yieldUnescaped("$message")
          }
        }
        if (testResult) {
          def errorMsg = escapeCdata(testResult.exception?.message ?: '')
          def stackTrace = escapeCdata(getStackTrace(testResult.exception))
          result(resultType: testResult.resultType ?: '', startTime: testResult.startTime, endTime: testResult.endTime) {
            def exception = testResult.exception
            if (exception?.message?.trim()) xml.mkp.yieldUnescaped("<errorMsg>$errorMsg</errorMsg>")
            if (exception) xml.mkp.yieldUnescaped("<stackTrace>$stackTrace</stackTrace>")
//
//            if ('junit.framework.ComparisonFailure'.equals(exception?.class?.name) ||
//                'org.junit.ComparisonFailure'.equals(exception?.class?.name)) {
//              def expected = escapeCdata(exception.fExpected)
//              def actual = escapeCdata(exception.fActual)
//              failureType('comparison')
//              xml.mkp.yieldUnescaped("<expected>$expected</expected>")
//              xml.mkp.yieldUnescaped("<actual>$actual</actual>")
//              return
//            }
//            try {
//              def fileComparisonFailure
//              if ('com.intellij.rt.execution.junit.FileComparisonFailure'.equals(exception?.class?.name)) {
//                fileComparisonFailure = exception
//              }
//              else if ('com.intellij.rt.execution.junit.FileComparisonFailure'.equals(exception?.cause?.class?.name)) {
//                fileComparisonFailure = exception.cause
//              }
//
//              if (fileComparisonFailure) {
//                def expected = escapeCdata(fileComparisonFailure.expected)
//                def actual = escapeCdata(fileComparisonFailure.actual)
//                def filePath = escapeCdata(fileComparisonFailure.filePath)
//                def actualFilePath
//                if (exception.hasProperty('actualFilePath')) {
//                  actualFilePath = escapeCdata(fileComparisonFailure.actualFilePath)
//                }
//                failureType('comparison')
//                xml.mkp.yieldUnescaped("<expected>$expected</expected>")
//                xml.mkp.yieldUnescaped("<actual>$actual</actual>")
//                xml.mkp.yieldUnescaped("<filePath>$filePath</filePath>")
//                if (actualFilePath) xml.mkp.yieldUnescaped("<actualFilePath>$actualFilePath</actualFilePath>")
//                return
//              }
//            }
//            catch (ignore) {
//            }
//            if ('junit.framework.AssertionFailedError'.equals(exception?.class?.name) || exception instanceof AssertionError) {
//              failureType('assertionFailed')
//              return
//            }
            if (exception) failureType('error')
          }
        }
      }
    }

    writeLog(writer.toString())
  }

  static String escapeCdata(String s) {
    return "<![CDATA[" + s?.getBytes("UTF-8")?.encodeBase64()?.toString() + "]]>";
  }

  static def wrap(String s) {
    if(!s) return s;
    s.replaceAll("\r\n|\n\r|\n|\r","<ijLogEol/>")
  }

  static def writeLog(s) {
    println String.format("<ijLog>%s</ijLog>", wrap(s))
  }

  static def logTestReportLocation(def report) {
    if(!report) return
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.event(type: 'reportLocation', testReport: report)
    writeLog(writer.toString());
  }

  static def logConfigurationError(aTitle, aMessage, boolean openSettings) {
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.event(type: 'configurationError', openSettings: openSettings) {
      title(aTitle)
      message(aMessage)
    }
    writeLog(writer.toString());
  }

  static def getStackTrace(Throwable t) {
    if(!t) return ''
    StringWriter sw = new StringWriter()
    t.printStackTrace(new PrintWriter(sw))
    sw.toString()
  }

  static def getName(TestDescriptor descriptor) {
    try {
      return descriptor.getDisplayName() // available starting from ver. 4.10.3
    } catch (Throwable ignore) {
      return descriptor.getName()
    }
  }

  static void appendTests(Gradle gradle) {
    gradle.taskGraph.whenReady { taskGraph ->
      taskGraph.allTasks.each { Task task ->
        if (task instanceof GameTestRunTask) {
          try {
//            task.doFirst {
//              try {
//                def urls = task.classpath.files.findAll {
//                  it.name == 'idea_rt.jar'
//                }.collect { it.toURI().toURL() }
//                def classLoader = Class.forName("org.gradle.launcher.daemon.bootstrap.DaemonMain").getClassLoader()
//                if (classLoader instanceof URLClassLoader) {
//                  for (URL url : urls) {
//                    classLoader.addURL(url)
//                  }
//                }
//                else {
//                  println("unable to enhance gradle daemon classloader with idea_rt.jar")
//                }
//              }
//              catch (RuntimeException all) {
//                all.printStackTrace()
//                println("unable to enhance gradle daemon classloader with idea_rt.jar")
//              }
//            }

            logTestReportLocation(task.reports?.html?.entryPoint?.path)
            configureTestEventLogging(task)
            task.testLogging.showStandardStreams = false
          }
          catch (all) {
            all.printStackTrace()
          }
        }
      }
    }
  }
}
