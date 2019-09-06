package com.xing.mars.operation.store

import java.util.concurrent.ConcurrentHashMap

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.option._
import com.xing.mars.operation.RoverWorker.RoverId
import com.xing.mars.operation.Status.{Free, OutOfPlateau, Waiting}
import com.xing.mars.operation.store.PlateauState.UpdateResult
import com.xing.mars.operation.store.PlateauState.UpdateResult.{
  Failure,
  Success
}
import com.xing.mars.operation.{ByCord, Cord, FieldStatus}

import scala.collection.JavaConverters._

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}.
  */
trait CordStore[F[_], K, V] {
  def getAll: F[List[(K, V)]]

  def get(k: K): F[Option[V]]

  def put(k: K, v: V): F[Unit]

  def putAll(map: Map[K, V]): F[Unit]

  def tryUpdate(k: K, v: V): F[UpdateResult[V]]

  def releasePrev(k: K): F[Unit]
}

class PlateauState[F[_]: Sync] private (store: ByCord[FieldStatus])
    extends CordStore[F, Cord, FieldStatus] {
  override def getAll: F[List[(Cord, FieldStatus)]] =
    Sync[F].delay(store.asScala.toList)

  override def put(k: Cord, v: FieldStatus): F[Unit] =
    Sync[F].delay(store.put(k, v)).void

  override def releasePrev(k: Cord): F[Unit] =
    put(k, FieldStatus(id = none[RoverId], status = Free))

  override def putAll(map: Map[Cord, FieldStatus]): F[Unit] =
    Sync[F].delay(store.putAll(map.asJava))

  override def get(k: Cord): F[Option[FieldStatus]] = Sync[F].delay {
    Option(store.get(k))
  }

  private def isOwner(owner: Option[RoverId]): Option[RoverId] => Boolean =
    id =>
      (owner, id) match {
        case (Some(ownerId), Some(id)) => ownerId == id
        case _                         => false
    }

  override def tryUpdate(k: Cord,
                         v: FieldStatus): F[UpdateResult[FieldStatus]] =
    Sync[F]
      .delay {
        store.compute(
          k,
          (c: Cord, status: FieldStatus) =>
            (Option(c), status) match {
              case (Some(_), FieldStatus(None, Free)) => v
              case (Some(_), FieldStatus(id @ Some(_), Waiting))
                  if isOwner(id)(v.id) =>
                v
              case (Some(_), f @ FieldStatus(Some(_), _)) => f
              case (_, _)                                 => FieldStatus(None, OutOfPlateau)
          }
        )
      }
      .map(status =>
        if (isOwner(v.id)(status.id)) Success(status) else Failure(status))
}

object PlateauState {

  sealed trait UpdateResult[V]

  object UpdateResult {
    case class Success[V](result: V) extends UpdateResult[V]
    case class Failure[V](fieldStatus: V) extends UpdateResult[V]
  }

  def apply[F[_]: Sync](store: ByCord[FieldStatus]): PlateauState[F] =
    new PlateauState(store)

  import cats.implicits._

  def from[F[_]: Sync](from: Map[Cord, FieldStatus]): F[PlateauState[F]] =
    for {
      state <- Sync[F].delay(apply(new ConcurrentHashMap[Cord, FieldStatus]()))
      _ <- state.putAll(from)
    } yield state
}
