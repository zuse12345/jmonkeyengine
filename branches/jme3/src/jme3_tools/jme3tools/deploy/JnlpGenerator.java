package jme3tools.deploy;

public class JnlpGenerator {

    public static void main(String[] args){
        JnlpParams p = JnlpParams.createSome();
        String params = generate(p);
        System.out.println(params);
    }

    public static final String generate(JnlpParams params){
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- jME3 App JNLP Template -->\n" +
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        // jnlp start
        sb.append("<jnlp spec=\"1.0+\" codebase=\"");
        sb.append(params.getCodebase());
        sb.append("\" href=\"JMEAPP.jnlp\">\n");

        // information start
        sb.append("    <information>\n");
        sb.append("       <title>").append(params.getTitle()).append("</title>\n");
        sb.append("       <vendor>").append(params.getVendor()).append("</vendor>\n");
        sb.append("       <homepage>").append(params.getHomepage()).append("</homepage>\n");
        sb.append("       <description>").append(params.getDescription()).append("</description>\n");
        sb.append("       <description kind=\"short\">").append(params.getShortDesc()).append("</description>\n");
        sb.append("       <icon href=\"").append(params.getIcon()).append("\" />\n");
        sb.append("    </information>\n");
        // information end

        // resources start
        sb.append("    <resources>\n");
        // j2se start
        sb.append("       <j2se version=\"").append(params.getJavaVersion()).append("+\"\n");
        sb.append("             java-vm-args=\"-Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -Dsun.java2d.d3d=false -Dsun.java2d.opengl=false -Dsun.java2d.pmoffscreen=false\"\n");
        sb.append("             href=\"http://java.sun.com/products/autodl/j2se\"\n");
        if (params.getInitHeapSize() > 0){
            sb.append("             initial-heap-size=\"").append(params.getInitHeapSize()).append("m\"\n");
        }
        if (params.getMaxHeapSize() > 0){
            sb.append("             max-heap-size=\"").append(params.getMaxHeapSize()).append("m\"\n");
        }
        sb.append("              />\n");
        // j2se end
        
        // jar start
        sb.append("       <jar href=\"").append(params.getMainJar()).append("\"\n");
        sb.append("            version=\"").append(params.getVersion()).append("\"\n");
        sb.append("            main=\"true\" />\n");
        // jar end
        sb.append("       <extension href=\"http://mfkarpg.110mb.com/jme3.jnlp\" />\n");

        sb.append("    </resources>\n");
        // resources end

        if (params.isAllowPermissions()){
            sb.append("    <security>\n");
            sb.append("        <all-permissions/>\n");
            sb.append("    </security>\n");
        }

        if (params.isApplet()){
            sb.append("    <applet-desc ");
        }else{
            sb.append("    <application-desc ");
        }
        sb.append("main-class=\"").append(params.getMainClass()).append("\"");
        if (params.isApplet()){
            sb.append(" width=\"").append(params.getWidth()).append("\"\n");
            sb.append(" height=\"").append(params.getHeight()).append("\"\n");
        }
        sb.append(" />\n");

        sb.append("</jnlp>");
        // end jnlp

        return sb.toString();
    }

}
