package com.longforus.voidbindcompiler;

import com.google.auto.service.AutoService;
import com.longforus.voidbindanno.BindView;
import com.longforus.voidbindapi.IBind;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

@AutoService(Processor.class)
//要处理的annotiation集合
@SupportedAnnotationTypes( { "com.longforus.voidbindanno.BindView" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class VoidBindCompiler extends AbstractProcessor {

    private ProcessingEnvironment mProcessingEnv;
    private SimpleDateFormat mDateFormat;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        //后续要用到
        mProcessingEnv = processingEnv;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,RoundEnvironment roundEnv) {
        //key为全类名 value为要处理的元素集合
        Map<String,List<VariableElement>> map = new HashMap<>();

        Set<? extends Element> annotatedWith = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : annotatedWith) {
            VariableElement e = (VariableElement)element;
            String fullClassName = getFullClassName(e);
            //如果map中包含就返回,否则调用lambda,生成value并用K保存到map中
            List<VariableElement> elements = map.computeIfAbsent(fullClassName,k -> new ArrayList<>());
            elements.add(e);
        }

        for (Map.Entry<String,List<VariableElement>> entry : map.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }

            String fullName = entry.getKey();
            int i = fullName.lastIndexOf(".");
            //获取外围元素的类型,toString()其实就是全类名
            TypeName enclosingTypeName = TypeName.get(entry.getValue().get(0).getEnclosingElement().asType());
            //生成方法
            MethodSpec.Builder methodBuild = MethodSpec.methodBuilder("bind").addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(void.class).addParameter(
                enclosingTypeName,"target");
            for (VariableElement element : entry.getValue()) {
                methodBuild.addStatement(
                    String.format(Locale.CHINA,"target.%s=target.findViewById(%d)",element.getSimpleName().toString(),element.getAnnotation(BindView.class).value()));
            }

            String enclosingClassName = fullName.substring(i + 1,fullName.length());
            //生成带泛型的接口类型名
            ParameterizedTypeName superinterface = ParameterizedTypeName.get(ClassName.get(IBind.class),enclosingTypeName);
            TypeSpec typeSpec = TypeSpec.classBuilder(enclosingClassName + "$VoidBind")
                                        .addModifiers(Modifier.PUBLIC,Modifier.FINAL)
                                        .addSuperinterface(superinterface)
                                        .addMethod(methodBuild.build()).build();

            String packageName = fullName.substring(0,i);
            JavaFile javaFile = JavaFile.builder(packageName,typeSpec).addFileComment("This class generate by VoidBind.Don't modify it,   - "+mDateFormat.format(new Date())).build();
            try {
                //写出到generated/source/kapt/debug/包名/目录下
                javaFile.writeTo(mProcessingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private String getFullClassName(VariableElement e) {
        //获取到持有e的外围元素,这里是Activity等
        TypeElement enclosingElement = (TypeElement)e.getEnclosingElement();
        //获取外围元素的包名
        String packageName = mProcessingEnv.getElementUtils().getPackageOf(enclosingElement).getQualifiedName().toString();
        return packageName + "." + enclosingElement.getSimpleName().toString();
    }
}
