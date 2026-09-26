package MoreBreedingOptimize.MD.book;

import MoreBreedingOptimize.MainClass;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.prefab.Manual;
import li.cil.manual.api.prefab.provider.NamespaceDocumentProvider;
import li.cil.manual.api.provider.DocumentProvider;
import li.cil.manual.api.util.Constants;

/**
 * Markdown Manual 手册 / 文档提供者的客户端注册。
 * <p>
 * 说明：markdown_manual 的注册表（manual / document_provider 等）只在该库
 * 的客户端初始化阶段创建，因此这里的所有注册都必须在客户端、且在
 * markdown_manual 构造完成之后执行（通过 neoforge.mods.toml 里
 * ordering = "AFTER" 保证加载顺序）。
 * <p>
 * {@link #register()} 由 {@link MainClass} 构造器在 dist=CLIENT 时调用一次。
 * 手册正文位于 assets/morebo/doc/&lt;语言&gt;/index.md。
 */
public final class BookManualClient {

    /** 手册注册表（markdown_manual:manual） */
    private static final DeferredRegister<ManualModel> MANUALS =
            DeferredRegister.create(MainClass.MODID, Constants.MANUAL_REGISTRY);

    /** morebo:manual —— 手册模型，{@link BookItem#getManualModel()} 返回它 */
    public static final RegistrySupplier<ManualModel> MANUAL =
            MANUALS.register("manual", Manual::new);

    /** morebo:manual_chemistry —— 化学书手册模型，{@link ChemistryBookItem#getManualModel()} 返回它 */
    public static final RegistrySupplier<ManualModel> MANUAL_CHEMISTRY =
            MANUALS.register("manual_chemistry", ChemistryManual::new);

    /** 文档提供者注册表（markdown_manual:document_provider） */
    private static final DeferredRegister<DocumentProvider> DOCUMENT_PROVIDERS =
            DeferredRegister.create(MainClass.MODID, Constants.DOCUMENT_PROVIDER_REGISTRY);

    /** morebo:doc —— 从 assets/morebo/doc/ 目录读取 Markdown 正文 */
    private static final RegistrySupplier<DocumentProvider> DOCUMENT_PROVIDER =
            DOCUMENT_PROVIDERS.register("doc", () -> new NamespaceDocumentProvider(MainClass.MODID, "doc"));

    private BookManualClient() {
    }

    /**
     * 把上面声明的条目真正写入 markdown_manual 的注册表。
     * 必须在客户端、mod 构造阶段调用（markdown_manual 客户端注册表已就绪时）。
     */
    public static void register() {
        MANUALS.register();
        DOCUMENT_PROVIDERS.register();
    }
}
