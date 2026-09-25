package MoreBreedingOptimize.MD.SM;

import MoreBreedingOptimize.MainClass;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public class ModDataAttachments {
    // 注册数据附件类型
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MainClass.MODID);

    // 定义一个布尔类型的附件，作为箭的标记
    public static final Supplier<AttachmentType<Boolean>> IS_LOVE_ARROW =
            ATTACHMENT_TYPES.register("is_love_arrow",
                    () -> AttachmentType.builder(() -> false).build());
}