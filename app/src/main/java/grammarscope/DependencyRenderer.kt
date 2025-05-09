package grammarscope

import android.graphics.Canvas
import android.widget.TextView
import com.bbou.brats.Annotation
import com.bbou.brats.AnnotationType
import grammarscope.document.Document

class DependencyRenderer(
    textView: TextView,
) : IRenderer {

    /**
     * Width
     */
    private var padWidth = textView.width - textView.paddingRight - textView.paddingLeft

    /**
     * Annotations to draw
     */
    internal lateinit var annotations: Map<AnnotationType, Collection<Annotation>>

    override fun annotate(document: Document, textView: TextView): Int {
        val (annotations, height) = DependencyAnnotator(textView).annotate(document)!!
        this.annotations = annotations
        return height
    }

    override fun paint(canvas: Canvas) {
        DependencyPainter.paintBoxes(canvas, this.annotations[AnnotationType.BOX] as Collection<Annotation.BoxAnnotation>)
        DependencyPainter.paintEdges(canvas, this.annotations[AnnotationType.EDGE] as Collection<Annotation.EdgeAnnotation>, this.padWidth.toFloat())
    }
}