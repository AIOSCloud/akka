/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.persistence.typed.javadsl

import akka.annotation.DoNotInherit
import akka.persistence.typed.internal.DisabledRetentionCriteria
import akka.persistence.typed.internal.SnapshotRetentionCriteriaImpl

abstract class RetentionCriteria {
  def asScala: akka.persistence.typed.scaladsl.RetentionCriteria
}

// FIXME docs
object RetentionCriteria {

  val disabled: RetentionCriteria = DisabledRetentionCriteria

  def snapshotEvery(numberOfEvents: Int, keepNSnapshots: Int): SnapshotRetentionCriteria =
    SnapshotRetentionCriteriaImpl(numberOfEvents, keepNSnapshots, deleteEventsOnSnapshot = false)

}

@DoNotInherit abstract class SnapshotRetentionCriteria extends RetentionCriteria {
  def withDeleteEventsOnSnapshot: SnapshotRetentionCriteria

}
