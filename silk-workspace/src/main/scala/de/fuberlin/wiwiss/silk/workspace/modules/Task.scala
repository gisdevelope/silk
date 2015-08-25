/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.fuberlin.wiwiss.silk.workspace.modules

import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import java.util.logging.Logger

import de.fuberlin.wiwiss.silk.runtime.activity.{Activity, ActivityControl, HasValue}
import de.fuberlin.wiwiss.silk.util.Identifier

import scala.reflect.ClassTag


/**
 * A task.
 *
 * @tparam DataType The data type that specifies the properties of this task.
 */
class Task[DataType: ClassTag](val name: Identifier, initialData: DataType,
                               module: Module[DataType]) {

  private val log = Logger.getLogger(getClass.getName)

  private def projectName: String = module.project.name

  @volatile
  private var currentData: DataType = initialData

  @volatile
  private var scheduledWriter: Option[ScheduledFuture[_]] = None

  private val activityList = module.plugin.activities(this, module.project)

  // Activity controls for all activities
  private var activityControls = Map[Class[_], ActivityControl[_]]()
  for(activity <- activityList)
    activityControls += ((activity.activityType, Activity(activity)))

  // Start autorun activities
  for(activity <- activityList if activity.autoRun)
    activityControls(activity.activityType).start()

  /**
   * Retrieves the current data of this task.
   */
  def data = currentData

  /**
   * Updates the data of this task.
   */
  def update(newData: DataType) = synchronized {
    // Update data
    currentData = newData
    // (Re)Schedule write
    for(writer <- scheduledWriter) {
      writer.cancel(false)
    }
    scheduledWriter = Some(Task.scheduledExecutor.schedule(Writer, Task.writeInterval, TimeUnit.SECONDS))
    log.info("Updated task '" + name + "'")
  }

  /**
   * All activities that belong to this task.
   */
  def activities: Traversable[ActivityControl[_]] = activityControls.valuesIterator.toSeq

  /**
   * Retrieves an activity by type.
   *
   * @tparam T The type of the requested activity
   * @return The activity control for the requested activity
   */
  def activity[T <: HasValue : ClassTag]: ActivityControl[T#ValueType] = {
    val requestedClass = implicitly[ClassTag[T]].runtimeClass
    activityControls.getOrElse(requestedClass, throw new NoSuchElementException(s"Task '$name' in project '$projectName' does not contain an activity of type '${requestedClass.getName}'. " +
                                                                                s"Available activities:\n${activityControls.keys.map(_.getName).mkString("\n ")}"))
                    .asInstanceOf[ActivityControl[T#ValueType]]
  }

  /**
   * Retrieves an activity by name.
   *
   * @param activityName The name of the requested activity
   * @return The activity control for the requested activity
   */
  def activity(activityName: String): ActivityControl[_] = {
    activityControls.values.find(_.name == activityName)
      .getOrElse(throw new NoSuchElementException(s"Task '$name' in project '$projectName' does not contain an activity named '$activityName'. " +
                                                  s"Available activities: ${activityControls.values.map(_.name).mkString(", ")}"))
  }

  private object Writer extends Runnable {
    override def run(): Unit = {
      // Write task
      module.provider.putTask(projectName, data)
      log.info(s"Persisted task '$name' in project '$projectName'")
      // Update caches
      for(activity <- activityList if activity.autoRun) {
        val activityControl = activityControls(activity.activityType)
        activityControl.cancel()
        activityControl.start()
      }
    }
  }
}

object Task {

  /* Do not persist updates more frequently than this (in seconds) */
  private val writeInterval = 5

  private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

}