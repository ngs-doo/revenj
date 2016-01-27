package org.revenj.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

@SupportedAnnotationTypes({"org.revenj.patterns.EventHandler"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RevenjProcessor extends AbstractProcessor {

	private TypeElement eventTypeElement;
	private DeclaredType eventDeclaredType;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		eventTypeElement = processingEnv.getElementUtils().getTypeElement("org.revenj.patterns.EventHandler");
		eventDeclaredType = processingEnv.getTypeUtils().getDeclaredType(eventTypeElement);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		Set<? extends Element> eventAnnotated = roundEnv.getElementsAnnotatedWith(eventTypeElement);
		if (!eventAnnotated.isEmpty()) {
			Map<String, List<String>> handlers = new HashMap<>();
			for (Element el : eventAnnotated) {
				if (!(el instanceof TypeElement)) {
					continue;
				}
				TypeElement element = (TypeElement) el;
				if (!hasPublicCtor(element)) {
					AnnotationMirror eventAnnotation = getAnnotation(element, eventDeclaredType);
					processingEnv.getMessager().printMessage(
							Diagnostic.Kind.ERROR,
							"EventHandler requires public constructor",
							element,
							eventAnnotation);
				} else {
					boolean foundImpl = false;
					for (TypeMirror iface : element.getInterfaces()) {
						String sign = iface.toString();
						if (!sign.startsWith("org.revenj.patterns.DomainEventHandler<")) {
							continue;
						}
						List<String> impl = handlers.get(sign);
						if (impl == null) {
							impl = new ArrayList<>();
							handlers.put(sign, impl);
						}
						if (element.getNestingKind().isNested()) {
							impl.add(element.getEnclosingElement().asType().toString() + "$" + element.getSimpleName().toString());
						} else {
							impl.add(element.asType().toString());
						}
						foundImpl = true;
					}
					if (!foundImpl) {
						AnnotationMirror eventAnnotation = getAnnotation(element, eventDeclaredType);
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.ERROR,
								"EventHandler annotation on " + element.toString() + " requires implementation of DomainEventHandler<T extends DomainEvent>",
								element,
								eventAnnotation);
					}
				}
			}
			if (!handlers.isEmpty()) {
				try {
					for (Map.Entry<String, List<String>> kv : handlers.entrySet()) {
						FileObject rfo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + URLEncoder.encode(kv.getKey(), "UTF-8"));
						BufferedWriter bw = new BufferedWriter(rfo.openWriter());
						for (String impl : kv.getValue()) {
							bw.write(impl);
							bw.newLine();
						}
						bw.close();
					}
				} catch (IOException e) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed saving event handler registrations");
				}
			}
		}
		return false;
	}

	private boolean hasPublicCtor(Element element) {
		for (ExecutableElement constructor : ElementFilter.constructorsIn(element.getEnclosedElements())) {
			if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkInterfaces(TypeElement element, TypeElement target) {
		for (TypeElement type : ElementFilter.typesIn(element.getEnclosedElements())) {
			String name = type.getSimpleName().toString();
		}
		return true;
	}

	private AnnotationMirror getAnnotation(Element element, DeclaredType annotationType) {
		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (processingEnv.getTypeUtils().isSameType(mirror.getAnnotationType(), annotationType)) {
				return mirror;
			}
		}
		return null;
	}
}
