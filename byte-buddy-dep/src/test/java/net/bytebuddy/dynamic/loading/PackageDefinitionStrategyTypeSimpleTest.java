package net.bytebuddy.dynamic.loading;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.test.utility.IntegrationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TemporaryFolder;

import java.io.Closeable;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PackageDefinitionStrategyTypeSimpleTest {

    private static final String FOO = "foo", BAR = "bar", QUX = "qux", BAZ = "baz";

    @Rule
    public MethodRule integrationRule = new IntegrationRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private PackageDefinitionStrategy.Definition definition;

    private URL sealBase;

    @Before
    public void setUp() throws Exception {
        sealBase = URI.create("file://foo").toURL();
        definition = new PackageDefinitionStrategy.Definition.Simple(FOO, BAR, QUX, BAZ, FOO + BAR, QUX + BAZ, sealBase);
    }

    @Test
    public void testIsDefined() throws Exception {
        assertThat(definition.isDefined(), is(true));
    }

    @Test
    public void testSpecificationTitle() throws Exception {
        assertThat(definition.getSpecificationTitle(), is(FOO));
    }

    @Test
    public void testSpecificationVersion() throws Exception {
        assertThat(definition.getSpecificationVersion(), is(BAR));
    }

    @Test
    public void testSpecificationVendor() throws Exception {
        assertThat(definition.getSpecificationVendor(), is(QUX));
    }

    @Test
    public void testImplementationTitle() throws Exception {
        assertThat(definition.getImplementationTitle(), is(BAZ));
    }

    @Test
    public void testImplementationVersion() throws Exception {
        assertThat(definition.getImplementationVersion(), is(FOO + BAR));
    }

    @Test
    public void testImplementationVendor() throws Exception {
        assertThat(definition.getImplementationVendor(), is(QUX + BAZ));
    }

    @Test
    @IntegrationRule.Enforce
    public void testSealBase() throws Exception {
        assertThat(definition.getSealBase(), is(sealBase));
    }

    @Test
    public void testSealedNotCompatibleToUnsealed() throws Exception {
        assertThat(definition.isCompatibleTo(getClass().getPackage()), is(false));
    }

    @Test
    public void testNonSealedIsCompatibleToUnsealed() throws Exception {
        assertThat(new PackageDefinitionStrategy.Definition.Simple(FOO, BAR, QUX, BAZ, FOO + BAR, QUX + BAZ, null)
                .isCompatibleTo(getClass().getPackage()), is(true));
    }

    @Test
    public void testNonSealedIsCompatibleToSealed() throws Exception {
        File file = temporaryFolder.newFile();
        try {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.SEALED, Boolean.TRUE.toString());
            URL url = new ByteBuddy().subclass(Object.class).name("foo.Bar").make().toJar(file, manifest).toURI().toURL();
            ClassLoader classLoader = new URLClassLoader(new URL[]{url}, null);
            try {
                Package definedPackage = classLoader.loadClass("foo.Bar").getPackage();
                assertThat(new PackageDefinitionStrategy.Definition.Simple(FOO, BAR, QUX, BAZ, FOO + BAR, QUX + BAZ, null)
                        .isCompatibleTo(definedPackage), is(false));
            } finally {
                if (classLoader instanceof Closeable) {
                    ((Closeable) classLoader).close();
                }
            }
        } finally {
            file.deleteOnExit();
        }
    }

    @Test
    public void testSealedIsCompatibleToSealed() throws Exception {
        File file = temporaryFolder.newFile();
        try {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.SEALED, Boolean.TRUE.toString());
            URL url = new ByteBuddy().subclass(Object.class).name("foo.Bar").make().toJar(file, manifest).toURI().toURL();
            ClassLoader classLoader = new URLClassLoader(new URL[]{url}, null);
            try {
                Package definedPackage = classLoader.loadClass("foo.Bar").getPackage();
                assertThat(new PackageDefinitionStrategy.Definition.Simple(FOO, BAR, QUX, BAZ, FOO + BAR, QUX + BAZ, url)
                        .isCompatibleTo(definedPackage), is(true));
            } finally {
                if (classLoader instanceof Closeable) {
                    ((Closeable) classLoader).close();
                }
            }
        } finally {
            file.deleteOnExit();
        }
    }
}
