package mrmd.morebreedingoptimize.mixin;

import li.cil.manual.client.document.segment.HeaderSegment;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 剥离 Markdown Manual 标题的下划线。
 * <p>
 * 库的 {@link HeaderSegment#getFormat()} 会对所有 # 标题强制追加
 * {@link ChatFormatting#UNDERLINE}（教科书式标题样式），配置无法关闭；
 * 这里在运行时把返回值里的下划线格式化码（§n）移除，标题仍保留字号放大。
 */
@Mixin(HeaderSegment.class)
public abstract class HeaderSegmentMixin {

    @Inject(method = "getFormat", at = @At("RETURN"), cancellable = true)
    private void morebo$removeHeaderUnderline(final CallbackInfoReturnable<String> cir) {
        final String format = cir.getReturnValue();
        if (format != null) {
            cir.setReturnValue(format.replace(ChatFormatting.UNDERLINE.toString(), ""));
        }
    }
}
