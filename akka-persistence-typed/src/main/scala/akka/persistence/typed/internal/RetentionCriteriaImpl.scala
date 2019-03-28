/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.persistence.typed.internal

import akka.persistence.typed.scaladsl
import akka.persistence.typed.javadsl

/**
 * Setup snapshot and event delete/retention behavior. Retention bridges snapshot
 * and journal behavior. This defines the retention criteria.
 *
 * @param snapshotEveryNEvents Snapshots are used to reduce playback/recovery times.
 *                             This defines when a new snapshot is persisted - on every N events.
 *                            `snapshotEveryNEvents` must be greater than 0.
 * @param keepNSnapshots      After a snapshot is successfully completed,
 *                             - if 2: retain last maximum 2 *`snapshot-size` events
 *                             and 3 snapshots (2 old + latest snapshot)
 *                             - if 0: all events with equal or lower sequence number
 *                             will not be retained. FIXME this comment is strange
 * @param deleteEventsOnSnapshot Opt-in ability to delete older events on successful
 *                               save of snapshot. Defaults to disabled.
 */
final case class SnapshotRetentionCriteriaImpl(
    snapshotEveryNEvents: Int,
    keepNSnapshots: Int,
    deleteEventsOnSnapshot: Boolean)
    extends javadsl.SnapshotRetentionCriteria
    with scaladsl.SnapshotRetentionCriteria {

  require(snapshotEveryNEvents > 0, s"snapshotEveryNEvents must be greater than 0, was [$snapshotEveryNEvents]")
  // FIXME should we support 0 or 1 as minimum?
  require(keepNSnapshots >= 0, s"keepNSnapshots must be greater than or equal to 0, was [$keepNSnapshots]")

  def snapshotWhen(currentSequenceNr: Long): Boolean =
    currentSequenceNr % snapshotEveryNEvents == 0

  /**
   * Delete Messages:
   *   {{{ toSequenceNr - keepNSnapshots * snapshotEveryNEvents }}}
   * Delete Snapshots:
   *   {{{ (toSequenceNr - 1) - (keepNSnapshots * snapshotEveryNEvents) }}}
   *
   * @param lastSequenceNr the sequence number to delete to if `deleteEventsOnSnapshot` is false
   */
  def toSequenceNumber(lastSequenceNr: Long): Long = {
    // Delete old events, retain the latest
    math.max(0, lastSequenceNr - (keepNSnapshots * snapshotEveryNEvents))
  }

  def deleteSnapshotsFromSequenceNr(toSeqNr: Long): Long = {
    // We could use 0 as fromSequenceNr to delete all older snapshots, but that might be inefficient for
    // large ranges depending on how it's implemented in the snapshot plugin. Therefore we use the
    // same window as defined for how much to keep in the retention criteria
    toSequenceNumber(toSeqNr)
  }

  override def withDeleteEventsOnSnapshot: SnapshotRetentionCriteriaImpl =
    copy(deleteEventsOnSnapshot = true)
}

case object DisabledRetentionCriteria extends javadsl.RetentionCriteria with scaladsl.RetentionCriteria
