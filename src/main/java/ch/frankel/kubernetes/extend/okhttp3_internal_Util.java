package ch.frankel.kubernetes.extend;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import okhttp3.internal.Util;
import okio.BufferedSource;
import okio.ByteString;

import java.io.IOException;
import java.nio.charset.Charset;

@TargetClass(Util.class)
public final class okhttp3_internal_Util {

    @Alias
    private static ByteString UTF_8_BOM;
    @Alias
    private static ByteString UTF_16_BE_BOM;
    @Alias
    private static ByteString UTF_16_LE_BOM;
    @Alias
    public static Charset UTF_8;
    @Alias
    private static Charset UTF_16_BE;
    @Alias
    private static Charset UTF_16_LE;

    @Substitute
    public static Charset bomAwareCharset(BufferedSource source, Charset charset) throws IOException {
        if (source.rangeEquals(0, UTF_8_BOM)) {
            source.skip(UTF_8_BOM.size());
            return UTF_8;
        }
        if (source.rangeEquals(0, UTF_16_BE_BOM)) {
            source.skip(UTF_16_BE_BOM.size());
            return UTF_16_BE;
        }
        if (source.rangeEquals(0, UTF_16_LE_BOM)) {
            source.skip(UTF_16_LE_BOM.size());
            return UTF_16_LE;
        }
        return charset;
    }
}
