package com.example.pdfdrawingapp.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pdfdrawingapp.R
import com.example.pdfdrawingapp.databinding.FragmentPdfviewBinding
import com.example.pdfdrawingapp.models.Tool
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.shockwave.pdfium.PdfDocument

class PdfViewFragment : Fragment(), OnPageChangeListener, OnLoadCompleteListener {
    private var _binding: FragmentPdfviewBinding? = null
    private val binding get() = _binding!!
    var pdfFileName: String? = null
    var pageNumber = 0

    val TAG = javaClass.simpleName
    private val args: PdfViewFragmentArgs by navArgs()
    private var selectedTool: Tool = Tool.Brush
    private var selectedColor: Int = R.color.red
    private var selectedBrushSize: Float = 20f
    private var selectedEraserSize: Float = 20f

    private val canvas = Canvas()
    private var bitmap: Bitmap? = null
    private val brushPaint = Paint().let {
        it.style = Paint.Style.FILL
        it.strokeJoin = Paint.Join.ROUND
        it.strokeWidth = selectedBrushSize * 2
        it.strokeCap = Paint.Cap.ROUND
        it
    }
    private val eraserPaint = Paint().let {
        it.style = Paint.Style.FILL
        it.strokeJoin = Paint.Join.ROUND
        it.strokeWidth = selectedEraserSize * 2
        it.strokeCap = Paint.Cap.ROUND
        it
    }
    private var lastBrushPoint: Point? = null
    private val bitmapList = ArrayList<Bitmap>()
    private var currentBitmapIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfviewBinding.inflate(inflater, container, false)

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUndoRedoButtons()

        displayFromSdcard()

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        eraserPaint.color =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                resources.getColor(R.color.white, null)
            else
                resources.getColor(R.color.white)

        initColorPicker()
        initToolButtons()
        initSlider()

        binding.canvasIv.setOnTouchListener { _, p1 ->
            if (bitmap == null) {
                clearBitmap()
            }

            when (selectedTool) {
                Tool.Brush -> {
                    drawWithBrushAction(event = p1)
                }

                Tool.Pail -> {
                    drawWithPailAction(event = p1)
                }

                Tool.Eraser -> {
                    eraseAction(event = p1)
                }

                Tool.Colors -> {
                    onSliderToolSelected(binding.brush, Tool.Brush)
                    drawWithBrushAction(event = p1)
                }
            }

            binding.canvasIv.setImageBitmap(bitmap)
            true
        }

        binding.saveBtn.setOnClickListener {
        }
    }

    private fun initUndoRedoButtons() {
        binding.undo.setOnClickListener {
            if (currentBitmapIndex > 0) {
                currentBitmapIndex--
                resetBitmap()
                updateUndoRedoButtonsState()
            }
        }
        binding.redo.setOnClickListener {
            if (currentBitmapIndex < bitmapList.lastIndex) {
                currentBitmapIndex++
                resetBitmap()
                updateUndoRedoButtonsState()
            }
        }
    }

    private fun updateUndoRedoButtonsState() {
        binding.undo.isEnabled = currentBitmapIndex > 0
        binding.redo.isEnabled = currentBitmapIndex < bitmapList.lastIndex
    }

    private fun initColorPicker() {
        closeColorPicker()

        val colorButtons = listOf(
            binding.colorOne,
            binding.colorTwo,
            binding.colorThree,
            binding.colorFour
        )

        val colors = listOf(
            R.color.red,
            R.color.green,
            R.color.blue,
            R.color.orange
        )

        val selectedColorIndex = colors.indexOf(selectedColor)
        onColorSelectedAction(colorButtons[selectedColorIndex], selectedColor)

        colorButtons.zip(colors).forEach { buttonColor ->
            buttonColor.first.setOnClickListener {
                onColorSelectedAction(buttonColor.first, buttonColor.second)
            }
        }
    }

    private fun displayFromSdcard() {
       val position = args.position
        pdfFileName = HomeFragment.fileList[position].name
        binding.pdfViewer.fromFile(HomeFragment.fileList[position.toInt()])
            .defaultPage(pageNumber)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(DefaultScrollHandle(requireContext()))
            .load()
    }

    private fun onColorSelectedAction(button: Button, color: Int) {
        selectColor(color)
        resetColorButtons()
        button
            .animate()
            .scaleX(ANIMATION_SCALE)
            .scaleY(ANIMATION_SCALE)
            .setDuration(ANIMATION_DURATION)
            .start()
    }

    private fun selectColor(color: Int) {
        selectedColor = color

        brushPaint.color =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                resources.getColor(color, null)
            else
                resources.getColor(color)

        binding.selectedColorDot.setBackgroundColor(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resources.getColor(color, null)
            } else {
                resources.getColor(color)
            }
        )
    }

    private fun resetColorButtons() {
        val buttons = listOf(
            binding.colorOne,
            binding.colorTwo,
            binding.colorThree,
            binding.colorFour
        )

        buttons.forEach { button ->
            if (button.scaleX == 1f) return@forEach

            button
                .animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(ANIMATION_DURATION)
                .start()
        }
    }

    private fun initToolButtons() {
        onToolSelected(binding.brush, Tool.Brush)

        val toolButtons = listOf(
            binding.brush,
            binding.pail,
            binding.eraser,
            binding.colors
        )

        val tools = listOf(
            Tool.Brush,
            Tool.Pail,
            Tool.Eraser,
            Tool.Colors
        )

        toolButtons.zip(tools).forEach { buttonTool ->
            buttonTool.first.setOnClickListener {
                when (buttonTool.second) {
                    Tool.Colors ->
                        onColorsToolSelected(buttonTool.first, buttonTool.second)

                    Tool.Brush, Tool.Eraser ->
                        onSliderToolSelected(buttonTool.first, buttonTool.second)

                    else ->
                        onToolSelected(buttonTool.first, buttonTool.second)
                }
            }
        }
    }

    private fun onSliderToolSelected(view: View, tool: Tool) {
        onToolSelected(view, tool)
        openSlider()
    }

    private fun onColorsToolSelected(view: View, tool: Tool) {
        onToolSelected(view, tool)
        openColorPicker()
    }

    private fun onToolSelected(view: View, tool: Tool) {
        selectedTool = tool
        resetToolButtons()
        view.isActivated = true
        closeColorPicker()
        closeSlider()
        updateSliderState()
    }

    private fun resetToolButtons() {
        val buttons = listOf(
            binding.brush,
            binding.pail,
            binding.eraser,
            binding.colors
        )

        buttons.forEach { button ->
            button.isActivated = false
        }
    }

    private fun initSlider() {
        updateSliderState()

        binding.slider.addOnChangeListener { _, value, _ ->
            if (selectedTool == Tool.Brush) {
                selectedBrushSize = value
                brushPaint.strokeWidth = selectedBrushSize * 2
            } else if (selectedTool == Tool.Eraser) {
                selectedEraserSize = value
                eraserPaint.strokeWidth = selectedEraserSize * 2
            }
        }
    }

    private fun updateSliderState() {
        binding.slider.visibility =
            when (selectedTool) {
                Tool.Brush -> {
                    binding.slider.value = selectedBrushSize
                    View.VISIBLE
                }

                Tool.Eraser -> {
                    binding.slider.value = selectedEraserSize
                    View.VISIBLE
                }

                else -> {
                    View.INVISIBLE
                }
            }
    }

    private fun clearBitmap() {
        binding.canvasIv.post {
            editBitmap(
                Bitmap.createBitmap(
                    binding.canvasIv.width,
                    binding.canvasIv.height,
                    Bitmap.Config.ARGB_8888
                )
            )
        }
    }

    private fun editBitmap(newBitmap: Bitmap) {
        bitmap = newBitmap
        bitmapList.add(newBitmap.copy(newBitmap.config, newBitmap.isMutable))
        canvas.setBitmap(bitmap)
    }

    private fun resetBitmap() {
        bitmap = Bitmap.createBitmap(
            binding.canvasIv.width,
            binding.canvasIv.height,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        canvas.drawBitmap(bitmapList[currentBitmapIndex], Matrix(Matrix()), null)
        binding.canvasIv.setImageBitmap(bitmap)
    }

    private fun addCurrentBitmapToHistory() {
        bitmap?.let { bitmap ->
            if (currentBitmapIndex != bitmapList.lastIndex) {
                for (i in bitmapList.lastIndex downTo (currentBitmapIndex + 1)) {
                    Log.d("indexes", "remove at $i")
                    bitmapList.removeAt(i)
                }
            }

            bitmapList.add(
                binding.canvasIv.drawToBitmap(
                    Bitmap.Config.ARGB_8888
                )
            )
            currentBitmapIndex++
            updateUndoRedoButtonsState()
        }
    }

    private fun drawWithBrushAction(event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                canvas.drawCircle(event.x, event.y, selectedBrushSize, brushPaint)
                lastBrushPoint = Point(event.x.toInt(), event.y.toInt())
            }

            MotionEvent.ACTION_MOVE -> {
                canvas.drawLine(
                    lastBrushPoint!!.x.toFloat(),
                    lastBrushPoint!!.y.toFloat(),
                    event.x,
                    event.y,
                    brushPaint
                )
                lastBrushPoint = Point(event.x.toInt(), event.y.toInt())
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                addCurrentBitmapToHistory()
            }
        }
    }

    private fun drawWithPailAction(event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                canvas.drawColor(brushPaint.color)
                addCurrentBitmapToHistory()
            }
        }
    }

    private fun eraseAction(event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                canvas.drawCircle(event.x, event.y, selectedEraserSize, eraserPaint)
                lastBrushPoint = Point(event.x.toInt(), event.y.toInt())
            }

            MotionEvent.ACTION_MOVE -> {
                canvas.drawLine(
                    lastBrushPoint!!.x.toFloat(),
                    lastBrushPoint!!.y.toFloat(),
                    event.x,
                    event.y,
                    eraserPaint
                )
                lastBrushPoint = Point(event.x.toInt(), event.y.toInt())
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                addCurrentBitmapToHistory()
            }
        }
    }

    private fun openColorPicker() {
        val colorButtons = listOf(
            binding.colorOne,
            binding.colorTwo,
            binding.colorThree,
            binding.colorFour
        )
        colorButtons.forEach { button ->
            button.visibility = View.VISIBLE
        }
    }

    private fun closeColorPicker() {
        val colorButtons = listOf(
            binding.colorOne,
            binding.colorTwo,
            binding.colorThree,
            binding.colorFour
        )
        colorButtons.forEach { button ->
            button.visibility = View.INVISIBLE
        }
    }

    private fun openSlider() {
        binding.slider.visibility = View.VISIBLE
    }

    private fun closeSlider() {
        binding.slider.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ANIMATION_DURATION: Long = 200
        private const val ANIMATION_SCALE = 1.3f
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNumber = page
        binding.appBarTitle.text = String.format("%s %s / %s", pdfFileName, page + 1, pageCount)
    }

    override fun loadComplete(nbPages: Int) {
        val meta = binding.pdfViewer.documentMeta
        printBookmarksTree(binding.pdfViewer.tableOfContents, "-")
    }

    fun printBookmarksTree(tree: List<PdfDocument.Bookmark>, sep: String) {
        for (b in tree) {
            Log.e(TAG, String.format("%s %s, p %d", sep, b.title, b.pageIdx))
            if (b.hasChildren()) {
                printBookmarksTree(b.children, "$sep-")
            }
        }
    }
}