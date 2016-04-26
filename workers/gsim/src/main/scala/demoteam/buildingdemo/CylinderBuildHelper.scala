package demoteam.buildingdemo

import improbable.corelib.math.Quaternion
import improbable.corelib.util.QuaternionUtils
import improbable.math.Vector3d

trait BuildHelperInterface {
  def getPositionForBlockAtIndex(iblock: Int): Vector3d

  def getRotationForBlockAtIndex(iblock: Int): Vector3d

  def maxBlocks: Int
}

class CylinderBuildHelper(cubeSide: Float, numBase: Int, numHeight: Int, heightMultiplier: Float, radMultiplier: Float) extends BuildHelperInterface {

  override def maxBlocks: Int = {
    numBase * numHeight
  }

  def testCross(a: Vector3d, b: Vector3d): Unit = {
    println(a + "x" + b + " = " + crossProduct(a, b))
  }

  def unitTest(): Unit = {
    println("testing cross product")
    testCross(Vector3d.unitX, Vector3d.unitY)
    testCross(Vector3d.unitY, Vector3d.unitZ)
    testCross(Vector3d.unitZ, Vector3d.unitX)
  }

  //unitTest()

  def getAngleForBlockAtIndex(iblock: Int): Double = {
    val iy = iblock / numBase
    val ixz = iblock % numBase
    (ixz.toDouble + (if (iy % 2 == 1) {
      0.5
    } else {
      0.0
    })) * (2 * math.Pi / numBase)
  }

  override def getPositionForBlockAtIndex(iblock: Int): Vector3d = {

    val iy = iblock / numBase

    val phiStep = 2 * math.Pi / numBase
    val phi = getAngleForBlockAtIndex(iblock)
    val r = (cubeSide / 2) * (1.0f + 1.0f / math.tan(phiStep / 2))

    Vector3d.unitY * (0.5 + iy) * cubeSide * heightMultiplier + (Vector3d.unitZ * math.sin(phi) + Vector3d.unitX * math.cos(phi)) * r * radMultiplier
  }

  def crossProduct(v0: Vector3d, v1: Vector3d): Vector3d = {
    Vector3d.unitX * (v0.y * v1.z - v0.z * v1.y) - Vector3d.unitY * (v0.x * v1.z - v0.z * v1.x) + Vector3d.unitZ * (v0.x * v1.y - v0.y * v1.x)
  }

  override def getRotationForBlockAtIndex(iblock: Int): Vector3d = {

    val v = getPositionForBlockAtIndex(iblock)
    val vXZ = v * (Vector3d.unitX + Vector3d.unitZ)

    val cosAng = math.min(math.max(vXZ.normalised.dot(Vector3d.unitZ), -1.0f), 1.0f)
    val ang = math.acos(cosAng)

    // now sign
    val signedAng = if (crossProduct(vXZ, Vector3d.unitZ).dot(Vector3d.unitY) < 0.0f) {
      ang
    }
    else {
      2 * math.Pi - ang
    }

    Vector3d.unitY * math.toDegrees(signedAng)
  }
}


class CuboidBuildHelper(cubeSide: Float, nx: Int, ny: Int, nz: Int) extends BuildHelperInterface {

  def numBaseHalf: Int = {
    nx + nz - 2
  }

  override def maxBlocks: Int = {
    2 * numBaseHalf * ny
  }

  override def getPositionForBlockAtIndex(iblock: Int): Vector3d = {

    val nbase = 2 * numBaseHalf
    val iy = iblock / nbase
    val ibase = iblock % nbase
    val ifrontBack = ibase / numBaseHalf
    val ixz = ibase % numBaseHalf

    val (startPos, dir) = if (ixz < (nx - 1)) {
      ((Vector3d.unitX * (1 - nx) + Vector3d.unitZ * (nz - 1)) * cubeSide / 2, Vector3d.unitX)
    }
    else {
      ((Vector3d.unitX * (nx - 1) + Vector3d.unitZ * (nz - 1)) * cubeSide / 2, -Vector3d.unitZ)
    }

    val (startPosRotated, dirRotated) = if (ifrontBack == 1) {
      val r180 = QuaternionUtils.fromAngleAxis(180.0f, Vector3d.unitY)
      (r180 * startPos, r180 * dir)
    }
    else {
      (startPos, dir)
    }
    val posMs = (startPosRotated + dirRotated * (ixz % (nx - 1)) + Vector3d.unitY * (1 + 2 * iy) / 2) * 1.1

    val r7 = QuaternionUtils.fromAngleAxis(7.0f * (iy % 2), Vector3d.unitY)
    r7 * posMs
  }

  override def getRotationForBlockAtIndex(iblock: Int): Vector3d = {

    val nbase = 2 * numBaseHalf
    val iy = iblock / nbase

    val r7 = QuaternionUtils.fromAngleAxis(7.0f * (iy % 2), Vector3d.unitY)
    QuaternionUtils.toEuler(r7)
  }
}

class PyramidBuildHelper(cubeSide: Float, n: Int) extends BuildHelperInterface {

  def square(n: Int): Int = {
    n * n
  }

  def sumNSquared(n: Int): Int = {
    //(1 to n).foldLeft(0)((total, i) => total + square(i))
    ((2*square(n)+ 3*n + 1)*n)/6
  }

  def occupancyToLayer(ilayer: Int): Int = {
    sumNSquared(n) - sumNSquared(n - ilayer)
  }

  override def maxBlocks: Int = {
    sumNSquared(n)
  }

  def getLayerForBlock(iblock: Int): Option[Int] = {
    (0 until n).find(ilayer => iblock - occupancyToLayer(ilayer) < square(n - ilayer))
  }

  override def getPositionForBlockAtIndex(iblocko: Int): Vector3d = {

    val iblock = iblocko%maxBlocks
    val layerOption = getLayerForBlock(iblock)
    val pos = layerOption.map {
      ilayer =>
        val nsideLayer = n - ilayer
        val nblocksLayer = square(nsideLayer)
        val iblockLayer = iblock - occupancyToLayer(ilayer)

        val ix = iblockLayer % nsideLayer
        val iz = iblockLayer / nsideLayer

        val posMs = Vector3d.unitX * cubeSide * (ix - ((nsideLayer - 1) * 0.5)) +
          Vector3d.unitZ * cubeSide * (iz - ((nsideLayer - 1) * 0.5)) +
          Vector3d.unitY * cubeSide * (0.5 + ilayer)

        val r7 = QuaternionUtils.fromAngleAxis(7.0f * ilayer, Vector3d.unitY)
        r7*posMs
    }

    pos.getOrElse(Vector3d.zero)
  }

  override def getRotationForBlockAtIndex(iblock: Int): Vector3d = {
    val layerOption = getLayerForBlock(iblock%maxBlocks)
    val ori = layerOption.map {
      ilayer =>
        QuaternionUtils.fromAngleAxis(7.0f * ilayer, Vector3d.unitY)
    }
    QuaternionUtils.toEuler(ori.getOrElse(QuaternionUtils.identity))
  }
}