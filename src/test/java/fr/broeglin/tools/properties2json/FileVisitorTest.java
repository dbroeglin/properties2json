package fr.broeglin.tools.properties2json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.plaf.FileChooserUI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileVisitorTest {
  FileVisitor visitor = new FileVisitor(null, null, "^(a.properties)$");
  Properties props;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void isPropertyFile_should_return_match_dot_properties() throws Exception {
    Path tmp = Files.createTempFile("test", ".properties");

    assertThat(visitor.isPropertyFile(tmp), is(true));
  }

  @Test
  public void isPropertyFile_should_not_match_if_not_dot_properties() throws Exception {
    Path tmp = Files.createTempFile("test", ".foo");

    assertThat(visitor.isPropertyFile(tmp), is(false));
  }

  @Test
  public void should_convert_empty_to_json() {
    assertThat(visitor.convertToJson(props), equalTo("{}"));
  }

  @Test
  public void should_convert_one_property_to_json() {
    props.put("a.b.c", "1");

    assertThat(visitor.convertToJson(props), equalTo("{\n  \"a.b.c\": \"1\"\n}"));
  }

  @Test
  public void should_convert_two_property_to_json() {
    props.put("a.b.c", "1");
    props.put("c.d.e", "2");

    assertThat(visitor.convertToJson(props), equalTo("{\n  \"c.d.e\": \"2\",\n  \"a.b.c\": \"1\"\n}"));
  }

  @Test
  public void should_rename_ending_with_properties() {
    assertThat(visitor.computeNewFileName(Paths.get("test.properties")), equalTo("__test.properties.json"));
  }

  @Test
  public void should_rename_ending_with_properties_and_does_not_match_excludes() {
    assertThat(visitor.computeNewFileName(Paths.get("aa.properties")), equalTo("__aa.properties.json"));
  }

  @Test
  public void should_not_rename_excluded() throws Exception {
    assertThat(visitor.isPropertyFile(Paths.get("a.properties")), equalTo(false));
  }

  @Test
  public void should_not_rename_if_not_ending_with_properties() throws Exception {
    assertThat(visitor.isPropertyFile(Paths.get("test.properties1")), equalTo(false));
  }

  @Test
  public void should_not_rename_file_starting_with_curly_bracket() throws Exception {
    Path path = preparePath("test.properties", " {}");

    assertThat(visitor.isPropertyFile(path), equalTo(false));
  }

  @Test
  public void should_not_rename_file_starting_with_square_bracket() throws Exception {
    Path path = preparePath("test.properties", " []");

    assertThat(visitor.isPropertyFile(path), equalTo(false));
  }
  
  @Test
  public void should_not_rename_file_starting_with_double_quote() throws Exception {
    Path path = preparePath("test.properties", "\n\t\"");

    assertThat(visitor.isPropertyFile(path), equalTo(false));
  }
  
  @Test
  public void should_rename_file_not_starting_with_square_bracket() throws Exception {
    Path path = preparePath("test.properties", "a = []");

    assertThat(visitor.isPropertyFile(path), equalTo(true));
  }
  
  @Test
  public void should_compute_relative_parent_as_empty() {
    FileVisitor visitor = new FileVisitor(Paths.get("source_dir"), null, "");

    assertThat(visitor.computeRelativeSourceParent(Paths.get("source_dir/a")),
        equalTo(Paths.get("")));
  }

  @Test
  public void should_compute_relative_parent_as_a() {
    FileVisitor visitor = new FileVisitor(Paths.get("source_dir"), null, "");

    assertThat(visitor.computeRelativeSourceParent(Paths.get("source_dir/a/b.txt")),
        equalTo(Paths.get("a")));

    visitor = new FileVisitor(Paths.get("/source_dir"), null, "");

    assertThat(visitor.computeRelativeSourceParent(Paths.get("/source_dir/a/b.txt")),
        equalTo(Paths.get("a")));
  }

  // plumbing
  @Before
  public void init() {
    this.props = new Properties();
  }

  private Path preparePath(String testFileName, String content) throws IOException, UnsupportedEncodingException {
    Path path = temp.newFile(testFileName).toPath();
    Files.write(path, content.getBytes("UTF-8"));
    return path;
  }
}