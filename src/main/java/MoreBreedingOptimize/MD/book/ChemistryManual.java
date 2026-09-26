package MoreBreedingOptimize.MD.book;

import li.cil.manual.api.prefab.Manual;

/**
 * 化学书（hbook）的手册模型。
 * <p>
 * 起始页指向 doc/&lt;语言&gt;/chemistry/index.md：与主手册 sbook 共用
 * {@link BookManualClient} 的文档提供者 morebo:doc，按子目录分文件
 * （doc/zh_cn/ 下放 index.md 与 chemistry/，互不干扰）。
 */
public class ChemistryManual extends Manual {

    @Override
    protected String getStartPage() {
        return "%LANGUAGE%/chemistry/index.md";
    }
}
