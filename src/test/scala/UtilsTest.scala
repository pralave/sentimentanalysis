import org.scalatest.FlatSpec

class UtilsTest extends FlatSpec {
  "Model" should "give correct cartesian product" in {
    val cp = com.github.shkesar.thealth.Utils.cartesianProduct(List(List(1, 2), List(1, 2)))

    assert(cp equals Seq(
      List(1, 1),
      List(1, 2),
      List(2, 1),
      List(2, 2)
    ))
  }
}
