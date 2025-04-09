package grammarscope

/**
 * Box renderer
 *
 * @author Bernard Bou
 */
abstract class ProtoRenderer : IRenderer {
    /**
     * Get topOffset
     *
     * @return the topOffset
     */
    override var topOffset: Int = 0
        protected set

    /**
     * Get topOffset
     *
     * @return the height
     */
    override var height: Int = 0
        protected set
}