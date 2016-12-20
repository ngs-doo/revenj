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
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.*;

@SupportedAnnotationTypes({"org.revenj.patterns.EventHandler", "javax.inject.Inject", "javax.inject.Singleton"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RevenjProcessor extends AbstractProcessor {

	private TypeElement eventTypeElement;
	private DeclaredType eventDeclaredType;
	private TypeElement injectTypeElement;
	private DeclaredType injectDeclaredType;
	private TypeElement singletonTypeElement;
	private DeclaredType singletonDeclaredType;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		eventTypeElement = processingEnv.getElementUtils().getTypeElement("org.revenj.patterns.EventHandler");
		eventDeclaredType = processingEnv.getTypeUtils().getDeclaredType(eventTypeElement);
		injectTypeElement = processingEnv.getElementUtils().getTypeElement("javax.inject.Inject");
		injectDeclaredType = injectTypeElement != null ? processingEnv.getTypeUtils().getDeclaredType(injectTypeElement) : null;
		singletonTypeElement = processingEnv.getElementUtils().getTypeElement("javax.inject.Singleton");
		singletonDeclaredType = singletonTypeElement != null ? processingEnv.getTypeUtils().getDeclaredType(singletonTypeElement) : null;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		Set<? extends Element> events = roundEnv.getElementsAnnotatedWith(eventTypeElement);
		Set<? extends Element> injects = injectTypeElement != null ? roundEnv.getElementsAnnotatedWith(injectTypeElement) : new HashSet<>();
		Set<? extends Element> singletons = singletonTypeElement != null ? roundEnv.getElementsAnnotatedWith(singletonTypeElement) : new HashSet<>();
		Map<String, List<String>> handlers = new HashMap<>();
		StringBuilder registrations = new StringBuilder();
		findEventHandlers(events, handlers);
		Set<TypeElement> added = findInjections(injects, registrations, singletons);
		registerTypes(singletons, added, registrations, true, singletonDeclaredType);
		if (!handlers.isEmpty()) {
			try {
				for (Map.Entry<String, List<String>> kv : handlers.entrySet()) {
					String path = "META-INF/services/" + URLEncoder.encode(kv.getKey(), "UTF-8");
					FileObject fo = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path);
					File file = new File(fo.toUri());
					Writer writer;
					if (!file.exists()) {
						fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path);
						writer = fo.openWriter();
					} else writer = new OutputStreamWriter(new FileOutputStream(file));
					for (String impl : kv.getValue()) {
						writer.write('\n');
						writer.write(impl);
					}
					writer.write('\n');
					writer.close();
				}
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed saving event handler registrations: " + e.getMessage());
			}
		}
		if (registrations.length() > 0) {
			try {
				FileObject fo = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "", "revenj_container_Registrations.java");
				File file = new File(fo.toUri());
				Writer writer;
				if (!file.exists()) {
					fo = processingEnv.getFiler().createSourceFile("revenj_container_Registrations");
					writer = fo.openWriter();
				} else writer = new OutputStreamWriter(new FileOutputStream(file));
				writer.write("public class revenj_container_Registrations implements org.revenj.extensibility.SystemAspect {\n");
				writer.write("  @Override\n  public void configure(org.revenj.extensibility.Container container) {\n");
				writer.write(registrations.toString());
				writer.write("\n  }\n}");
				writer.close();
				fo = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/org.revenj.extensibility.SystemAspect");
				file = new File(fo.toUri());
				if (file.exists()) {
					List<String> content = Files.readAllLines(file.toPath());
					if (!content.contains("revenj_container_Registrations")) {
						writer = new OutputStreamWriter(new FileOutputStream(file, true));
						writer.write("\nrevenj_container_Registrations\n");
						writer.close();
					}
				} else {
					writer = new OutputStreamWriter(new FileOutputStream(file));
					writer.write("\nrevenj_container_Registrations\n");
					writer.close();
				}
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed saving container registrations: " + e.getMessage());
			}
		}
		return false;
	}

	private void findEventHandlers(Set<? extends Element> events, Map<String, List<String>> handlers) {
		for (Element el : events) {
			if (!(el instanceof TypeElement)) {
				continue;
			}
			TypeElement element = (TypeElement) el;
			if (!hasPublicCtor(element)) {
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.ERROR,
						"EventHandler requires public constructor",
						element,
						getAnnotation(element, eventDeclaredType));
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
					processingEnv.getMessager().printMessage(
							Diagnostic.Kind.ERROR,
							"EventHandler annotation on " + element.toString() + " requires implementation of DomainEventHandler<T extends DomainEvent>",
							element,
							getAnnotation(element, eventDeclaredType));
				}
			}
		}
	}

	private Set<TypeElement> findInjections(Set<? extends Element> injects, StringBuilder registrations, Set<? extends Element> singletons) {
		Set<TypeElement> registered = new HashSet<>();
		for (Element el : injects) {
			Element p = el.getEnclosingElement();
			if (!(el instanceof ExecutableElement) || !(p instanceof TypeElement)) {
				continue;
			}
			ExecutableElement element = (ExecutableElement) el;
			TypeElement parent = (TypeElement) p;
			if (!element.getModifiers().contains(Modifier.PUBLIC)
					|| !parent.getModifiers().contains(Modifier.PUBLIC)) {
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.WARNING,
						"@Inject used in '" + parent.asType() + "' can only be used on a public constructor in a public type",
						element,
						getAnnotation(element, injectDeclaredType));
			} else if (parent.getTypeParameters().size() > 0) {
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.WARNING,
						"@Inject used on '" + parent.asType() + "' will be handled by reflection",
						element,
						getAnnotation(element, injectDeclaredType));
			} else {
				int position = registrations.length();
				registrations.append("    container.registerFactory(");
				registrations.append(parent);
				registrations.append(".class, c -> new ");
				registrations.append(parent);
				registrations.append("(");
				for (VariableElement ve : element.getParameters()) {
					String typeName = ve.asType().toString();
					int genInd = typeName.indexOf('<');
					String containerType = genInd > 0 ? typeName.substring(0, genInd) : typeName;
					TypeElement argType = processingEnv.getElementUtils().getTypeElement(containerType);
					if (!argType.getModifiers().contains(Modifier.PUBLIC)) {
						processingEnv.getMessager().printMessage(
								Diagnostic.Kind.WARNING,
								"Arguments for constructor with @Inject must be public. '" + ve.asType() + "' is not public.",
								element,
								getAnnotation(element, injectDeclaredType));
						registrations.setLength(position);
						return registered;
					}
					if (genInd > 0) {
						if (!checkGenericArguments(typeName, element)) {
							registrations.setLength(position);
							return registered;
						}
						registrations.append("new org.revenj.patterns.Generic<");
						registrations.append(typeName);
						registrations.append(">(){}.resolve(c)");
					} else {
						registrations.append("c.resolve(");
						registrations.append(typeName);
						registrations.append(".class)");
					}
					registrations.append(",");
				}
				if (element.getParameters().size() > 0) {
					registrations.setLength(registrations.length() - 1);
				}
				if (singletons.contains(parent)) {
					registrations.append("), true);\n");
				} else {
					registrations.append("), false);\n");
				}
				registered.add(parent);
			}
		}
		return registered;
	}

	private boolean checkGenericArguments(String typeName, ExecutableElement element) {
		int genInd = typeName.indexOf('<');
		if (genInd == -1) return true;
		String[] args = typeName.substring(genInd + 1, typeName.length() - 1).split(",");
		for (String t : args) {
			TypeElement argType = processingEnv.getElementUtils().getTypeElement(t.trim());
			if (!argType.getModifiers().contains(Modifier.PUBLIC)) {
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.WARNING,
						"Arguments for constructor with @Inject must be public. '" + t.trim() + "' is not public.",
						element,
						getAnnotation(element, injectDeclaredType));
				return false;
			}
			if (!checkGenericArguments(t, element)) return false;
		}
		return true;
	}

	private void registerTypes(
			Set<? extends Element> types,
			Set<TypeElement> injections,
			StringBuilder registrations,
			boolean singleton,
			DeclaredType declaredType) {
		for (Element el : types) {
			if (!(el instanceof TypeElement)) {
				continue;
			}
			TypeElement element = (TypeElement) el;
			if (!element.getModifiers().contains(Modifier.PUBLIC)) {
				processingEnv.getMessager().printMessage(
						Diagnostic.Kind.WARNING,
						(singleton ? "@Singleton" : "@Transient") + " used on '" + element.asType() + "' can only be used on a public type",
						element,
						getAnnotation(element, declaredType));
			} else if (!injections.contains(element)) {
				registrations.append("    container.register(");
				registrations.append(element.asType());
				registrations.append(".class, ");
				registrations.append(singleton ? "true);" : "false);\n");
			}
		}
	}

	private boolean hasPublicCtor(Element element) {
		for (ExecutableElement constructor : ElementFilter.constructorsIn(element.getEnclosedElements())) {
			if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
				return true;
			}
		}
		return false;
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
