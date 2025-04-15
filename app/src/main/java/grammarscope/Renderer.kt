package grammarscope

import android.graphics.Canvas
import android.widget.TextView
import com.bbou.brats.Annotation
import com.bbou.brats.AnnotationType
import grammarscope.document.Document

class Renderer(
    val textView: TextView,
    val renderAsCurves: Boolean
) : IRenderer {

    /**
     * Width
     */
    private var padWidth = textView.width - textView.paddingRight - textView.paddingLeft

    /**
     * Annotations to draw
     */
    internal lateinit var annotations: Map<AnnotationType, Collection<Annotation>>

    override fun layout(document: Document, textView: TextView): Int {
        val (annotations, height) = SemanticGraphRenderer(textView, renderAsCurves).annotate(document)!!
        this.annotations = annotations
        return height
    }

    override fun paint(canvas: Canvas) {
        SemanticGraphPainter.paintBoxes(canvas, this.annotations[AnnotationType.BOX] as Collection<Annotation.BoxAnnotation>)
        SemanticGraphPainter.paintEdges(canvas, this.annotations[AnnotationType.EDGE] as Collection<Annotation.EdgeAnnotation>, this.padWidth, this.renderAsCurves)
    }
}