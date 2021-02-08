package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.jstl.JstlTemplateManager;
import com.brentcroft.tools.jstl.MapBindings;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Getter
public class SchemaObject {
  protected final List<ElementObject> rootObjects = new LinkedList<>();
  protected final List<ComplexTypeObject> complexTypes = new LinkedList<>();
  protected final List<SimpleTypeObject> simpleTypes = new LinkedList<>();
  @Setter
  private String systemId;

  public String toString() {
    return format(
        "rootElements: %n%s %n" + "complexTypes: %n%s %n" + "simpleTypes: %n%s",
        rootObjects
            .stream()
            .map(SchemaItem::toString)
            .collect(Collectors.joining("\n")),
        complexTypes
            .stream()
            .map(SchemaItem::toString)
            .collect(Collectors.joining("\n")),
        simpleTypes
            .stream()
            .map(SchemaItem::toString)
            .collect(Collectors.joining("\n"))
    );
  }


  public String generateSource(Mutator rootMutator, String templateUri, String packageName) {
    List<Mutator> stepTables = new ArrayList<>();

    rootMutator.detectTables(stepTables);

    JstlTemplateManager jstl = new JstlTemplateManager();

    // allow the escaping using &#125; -> } etc.

    Pattern p = Pattern.compile("&#\\d{3};");

    jstl
        .getELTemplateManager()
        .setValueExpressionFilter(v -> ofNullable(v)
            .filter(o -> o instanceof String)
            .map(Object::toString)
            .filter(s -> p.matcher(s).find())
            .map(s -> (Object) s.replace("&#125;", "}"))
            .orElse(v)
        );

    return jstl
        .expandUri(
            templateUri,
            new MapBindings()
                .withEntry("packageName", packageName)
                .withEntry("rootClass", rootMutator.getContext())
                .withEntry("root", rootMutator)
                .withEntry("steps", stepTables)
        );
  }
}
