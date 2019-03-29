/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.persistence.typed.scaladsl

import akka.annotation.DoNotInherit
import akka.persistence.typed.internal.DisabledRetentionCriteria
import akka.persistence.typed.internal.SnapshotRetentionCriteriaImpl

trait RetentionCriteria {
  def asJava: akka.persistence.typed.javadsl.RetentionCriteria
}

// FIXME docs
object RetentionCriteria {

  val disabled: RetentionCriteria = DisabledRetentionCriteria

  def snapshotEvery(numberOfEvents: Int, keepNSnapshots: Int): SnapshotRetentionCriteria =
    SnapshotRetentionCriteriaImpl(numberOfEvents, keepNSnapshots, deleteEventsOnSnapshot = false)

}

@DoNotInherit trait SnapshotRetentionCriteria extends RetentionCriteria {
  def withDeleteEventsOnSnapshot: SnapshotRetentionCriteria
}
