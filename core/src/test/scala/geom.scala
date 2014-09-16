package archery

import scala.collection.mutable.ArrayBuffer
import scala.math.{ceil, min, max}
import scala.util.Random.nextFloat
import scala.math.{abs, pow, sqrt}

import org.scalacheck.Arbitrary._
import org.scalatest._
import prop._

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary

object GeomUtil {

  case class Coord(n: Float)

  def isFinite(n: Float) = !n.isInfinite && !n.isNaN

  implicit val arbcoord: Arbitrary[Coord] =
    Arbitrary(arbitrary[Float].suchThat(isFinite).map(n => Coord(n % 30000000)))

  implicit val arbpoint = Arbitrary(for {
    x <- arbitrary[Coord].map(_.n)
    y <- arbitrary[Coord].map(_.n)
  } yield Point(x, y))

  implicit val arbbox = Arbitrary(for {
    x1 <- arbitrary[Coord].map(_.n)
    x2 <- arbitrary[Coord].map(_.n)
    y1 <- arbitrary[Coord].map(_.n)
    y2 <- arbitrary[Coord].map(_.n)
  } yield Box((x1 min x2), (y1 min y2), (x1 max x2), (y1 max y2)))

  implicit val arbgeom: Arbitrary[Geom] =
    Arbitrary(arbitrary[Either[Point, Box]].map(_.fold(identity, identity)))
}

class GeomCheck extends PropSpec with Matchers with GeneratorDrivenPropertyChecks {

  import GeomUtil._

  property("points: distance works") {
    forAll { (x1: Float, y1: Float, x2: Float, y2: Float) =>
      (Point(x1, y1) distance Point(x2, y2)) shouldBe sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2))
    }
  }

  property("points: distance is commutative") {
    forAll { (p1: Point, p2: Point) => (p1 distance p2) shouldBe (p2 distance p1) }
  }

  property("points: distance = maxDistance") {
    forAll { (p1: Point, p2: Point) => (p1 distance p2) shouldBe (p1 maxDistance p2) }
  }

  property("box: distance works") {
    forAll { (box: Box, p: Point) =>
      val m = box.corners.map(_ distanceSquared p).min
      val d = box.distanceSquared(p)

      d should be <= m
      if (box contains p) {
        d shouldBe 0F
      } else {
        val dx = if (box.x <= p.x && p.x <= box.x2) 0F else (box.x - p.x).abs min (p.x - box.x2).abs
        val dy = if (box.y <= p.y && p.y <= box.y2) 0F else (box.y - p.y).abs min (p.y - box.y2).abs
        val sq = dx * dx + dy * dy
        (d / sq) should be >= 1.0 - 1e-5
        (d / sq) should be <= 1.0 + 1e-5
      }
    }
  }

  property("box: maxDistance works") {
    forAll { (b: Box, p: Point) =>
      (b maxDistance p) shouldBe b.corners.map(_ distance p).max
    }
  }
}
