import org.junit.Test

class GroovyTest {

	@Test
	void convertUpperToCamel() {
		def original = 'MY_ENUM'
		def expected = 'onMyEnum'

		def actual = 'on' + original.split('_').collect {it.toLowerCase().capitalize()}.join('')

		assert expected == actual
	}
}
