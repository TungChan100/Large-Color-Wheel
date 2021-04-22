package  wcc


import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.JSpinner.DefaultEditor

/**
 * メインウィンドウ
 */
class MainWindow : JFrame() {
    private val hueCircle: HueCircle = HueCircle(DEFAULT_COLOR_NUM).also {
        it.onHsChangeListener = ::onHsChange
    }
    private val svSection: SvSection = SvSection().also {
        it.onSvChangeListener = ::onSvChange
    }
    private val sliderPanel: SliderPanel = SliderPanel().also {
        it.onHsvChangeListener = ::onHsvChange
    }
    private val samplePanel: ColorSamplePanel = ColorSamplePanel(DEFAULT_COLOR_NUM)
    private val hexColorArea: JTextArea = JTextArea().also {
        it.columns = 7 // #xxxxxxで7文字
        it.isEditable = false // 編集不可
    }
    private val decColorArea: JTextArea = JTextArea().also {
        it.columns = 18 // rgb(xxx, xxx, xxx)で18文字
        it.isEditable = false
    }
    private val divisionSpinner: JSpinner = JSpinner(SpinnerNumberModel().also {
        it.minimum = 1
        it.maximum = 360
    }).also {
        it.value = 1
        (it.editor as DefaultEditor).let { editor ->
            editor.textField.columns = 3
            editor.textField.isFocusable = false
        }
    }
    private val reverseCheck: JCheckBox = JCheckBox("逆")

//  private val rgbInput: JTextField = JTextField().also {

    private val rgbInput: JTextArea = JTextArea().also {


        it.isEditable = false
        it.rows = 3
        it.columns = 7

        it.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                //onEditRgbInput()
            }
        })
    }
    private var currentColor: Int = 0

    init {



        title = "Color Wheel"
        setSize(900, 650)
        isResizable=true

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        background = Color.white

        setUpFont()
        setUpSpinner()

        val contentPane = JPanel()
        val gbl = GridBagLayout()
        contentPane.layout = gbl

        hueCircle.also {
            gbl.setConstraints(it, makeGridBagConstraints(0, 0, 1, 2))
            contentPane.add(it)
        }
        svSection.also {
            gbl.setConstraints(it, makeGridBagConstraints(1, 0, 1, 1))
            contentPane.add(it)
        }



               makeControlPanel().also {
                   gbl.setConstraints(it, makeGridBagConstraints(1, 1, 1, 1))
                   contentPane.add(it)
               }

/*

               makeScrollPane().also {
                   gbl.setConstraints(it, makeGridBagConstraints(2, 0, 1, 2))
                   contentPane.add(it)
               }
               */

        setContentPane(contentPane)

        isVisible = true
        setColors()

        hueCircle.setDivision(1)
    }

    private fun makeGridBagConstraints(x: Int, y: Int, width: Int, height: Int) = GridBagConstraints().also {
        it.anchor = GridBagConstraints.NORTHWEST
        it.gridx = x
        it.gridy = y
        it.gridwidth = width
        it.gridheight = height

        it.insets =   Insets(3,18,3,3);
    }

    private fun setUpFont() {
        val monospace = Font("Monospaced", Font.PLAIN, 12)

        val bigFont = Font("Monospaced", Font.BOLD, 72)

        rgbInput.columns = 6

        rgbInput.font = bigFont
        hexColorArea.font = monospace
        decColorArea.font = monospace
    }

    private fun setUpSpinner() {
        divisionSpinner.addChangeListener {
            // 分割数用Spinnerの変化
            hueCircle.setDivision(divisionSpinner.value as Int)
            setColors()
        }
        reverseCheck.addChangeListener {
            hueCircle.setReverse(reverseCheck.isSelected)
            setColors()
        }
    }

    private fun makeSettingPanel() = JPanel(FlowLayout()).also {
       // it.add(JLabel(""))
        it.add(rgbInput)
        // it.add(JLabel("数"))
        //it.add(divisionSpinner)
       // it.add(reverseCheck)
    }



    private fun makeControlPanel() = JPanel(BorderLayout()).also {
        it.add(makeSettingPanel(), BorderLayout.NORTH)
       // it.add(sliderPanel, BorderLayout.CENTER)
    }

    private fun onEditRgbInput() {
        val hexText = rgbInput.text
        var color = convertHexToRgb(hexText)
        if (color < 0) {
            rgbInput.background = Color.PINK
        } else {
            color = color.setAlpha(255)
            rgbInput.background = Color.WHITE
            if (color == currentColor) {
                return
            }
            val rgb = ColorUtils.toRGB(color)
            sliderPanel.setRgb(rgb[0], rgb[1], rgb[2])
        }
    }

    private fun setRgbInput(color: Int) {

        rgbInput.background =    Color(color)



        val rgb = ColorUtils.toRGBInt(color)



        val hexText = String.format("%02X%02X%02X", rgb[0], rgb[1], rgb[2])
        if (rgbInput.text.toUpperCase() != hexText) {
            rgbInput.text = hexText
        }





    }

    /**
     * 16進数テキストをRGB値に変更
     *
     * @param hexText 16進数表現の色
     * @return RGB値
     */
    private fun convertHexToRgb(hexText: String): Int {
        if (hexText.length != 6) {
            return -1
        }
        val text = hexText.toLowerCase()
        var color = 0
        for (i in 0..5) {
            color = color shl 4
            val c = text[i]
            color += when (c) {
                in '0'..'9' -> c - '0'
                in 'a'..'f' -> c - 'a' + 10
                else -> return -1
            }
        }
        return color
    }

    /**
     * ピックアップした色をUIに反映する
     */
    private fun setColors() {
        val colors = hueCircle.colors
        currentColor = colors[0]
        setRgbInput(currentColor)
        // サンプル一覧を更新
        samplePanel.setColors(colors)
        // 16進数表記と10進数表記まとめて作成
        val hexSb = StringBuilder()
        val decSb = StringBuilder()
        colors.forEach {
            val rgb = ColorUtils.toRGBInt(it)
            val hexText = String.format("#%02X%02X%02X%n", rgb[0], rgb[1], rgb[2])
            hexSb.append(hexText)
            val decText = String.format("rgb(%d, %d, %d)%n", rgb[0], rgb[1], rgb[2])
            decSb.append(decText)
        }
        // テキストエリアへ反映
        hexColorArea.text = hexSb.toString().trim { it <= ' ' }
        decColorArea.text = decSb.toString().trim { it <= ' ' }
    }

    private fun onSvChange(hue: Float, saturation: Float, value: Float) {
        // SV断面の変化
        hueCircle.setHsv(hue, saturation, value)
        sliderPanel.setHsv(hue, saturation, value)
        setColors()
    }

    private fun onHsChange(hue: Float, saturation: Float, value: Float) {
        // HSサークルの変化
        svSection.setHsv(hue, saturation, value)
        sliderPanel.setHsv(hue, saturation, value)
        setColors()
    }

    private fun onHsvChange(hue: Float, saturation: Float, value: Float) {
        // スライダーによる変化
        hueCircle.setHsv(hue, saturation, value)
        svSection.setHsv(hue, saturation, value)
        setColors()
    }

    companion object {

        private const val DEFAULT_COLOR_NUM = 12

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: UnsupportedLookAndFeelException) {
                e.printStackTrace()
            }
            MainWindow()
        }
    }
}
