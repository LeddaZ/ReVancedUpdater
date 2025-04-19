package it.leddaz.revancedupdater.utils.plugins

import android.graphics.Typeface
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.IndentedCodeBlock

class CodeTypefacePlugin(private val codeTypeface: Typeface) : AbstractMarkwonPlugin() {
    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(Code::class.java, CodeVisitor())
        builder.on(FencedCodeBlock::class.java, FencedCodeBlockVisitor())
        builder.on(IndentedCodeBlock::class.java, IndentedCodeBlockVisitor())
    }

    private inner class CodeVisitor : MarkwonVisitor.NodeVisitor<Code> {
        override fun visit(visitor: MarkwonVisitor, code: Code) {
            val length = visitor.length()
            visitor.builder().append(code.literal)

            // Apply the typeface to inline code
            visitor.setSpans(length, CodeTypefaceSpan(codeTypeface))
        }
    }

    private inner class FencedCodeBlockVisitor : MarkwonVisitor.NodeVisitor<FencedCodeBlock> {
        override fun visit(
            visitor: MarkwonVisitor,
            fencedCodeBlock: FencedCodeBlock
        ) {
            val length = visitor.length()
            visitor.builder().append(fencedCodeBlock.literal)

            // Apply the typeface to fenced code blocks
            visitor.setSpans(length, CodeTypefaceSpan(codeTypeface))
        }
    }

    private inner class IndentedCodeBlockVisitor : MarkwonVisitor.NodeVisitor<IndentedCodeBlock> {
        override fun visit(
            visitor: MarkwonVisitor,
            indentedCodeBlock: IndentedCodeBlock
        ) {
            val length = visitor.length()
            visitor.builder().append(indentedCodeBlock.literal)

            // Apply the typeface to indented code blocks
            visitor.setSpans(length, CodeTypefaceSpan(codeTypeface))
        }
    }
}