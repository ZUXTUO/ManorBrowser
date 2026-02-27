/**
 * JavaScript 脚本注入辅助类
 * 负责生成用于向网页注入的自定义 JS 脚本，目前主要包括：
 * 1. 登录表单检测与密码保存 (通过拦截 submit 事件)。
 * 2. 账号密码自动填充。
 */
package com.olsc.manorbrowser.utils;

public class JSInjector {

    /**
     * 改进的登录检测脚本
     * 此脚本会注入到网页中并监听所有 form 的 submit 事件。
     * 探测到含有密码类型的输入框提交时，通过拦截 prompt 机制将账号信息回传给 Android 原生层。
     */
    public static final String INJECT_LOGIN_DETECT =
        "(function() {" +
        "    if (window.manorInjected) return;" +
        "    window.manorInjected = true;" +
        "    " +
        "    function detectAndSave() {" +
        "        var forms = document.querySelectorAll('form');" +
        "        forms.forEach(function(form) {" +
        "            form.addEventListener('submit', function(e) {" +
        "                var passInputs = form.querySelectorAll('input[type=\"password\"]');" +
        "                if (passInputs.length === 0) return;" +
        "                " +
        "                var passInput = passInputs[0];" +
        "                if (!passInput.value) return;" +
        "                " +
        "                var username = '';" +
        "                var inputs = Array.from(form.querySelectorAll('input'));" +
        "                " +
        "                // 尝试定位密码框之前的文本输入框作为用户名" +
        "                for (var i = 0; i < inputs.length; i++) {" +
        "                    var inp = inputs[i];" +
        "                    var type = inp.type.toLowerCase();" +
        "                    var name = (inp.name || '').toLowerCase();" +
        "                    var id = (inp.id || '').toLowerCase();" +
        "                    " +
        "                    if (inp === passInput) break;" +
        "                    " +
        "                    if (type === 'text' || type === 'email' || type === 'tel' || " +
        "                        name.includes('user') || name.includes('email') || name.includes('login') || name.includes('account') ||" +
        "                        id.includes('user') || id.includes('email') || id.includes('login') || id.includes('account')) {" +
        "                        if (inp.value) username = inp.value;" +
        "                    }" +
        "                }" +
        "                " +
        "                // 发送提取到的凭传：格式为 MANOR_SAVE_PASS|URL|USERNAME|PASSWORD" +
        "                if (username && passInput.value) {" +
        "                    prompt('MANOR_SAVE_PASS|' + window.location.href + '|' + username + '|' + passInput.value);" +
        "                }" +
        "            }, true);" +
        "        });" +
        "    }" +
        "    " +
        "    detectAndSave();" +
        "    " +
        "    // 针对动态加载表单的延迟检测" +
        "    setTimeout(detectAndSave, 1000);" +
        "    setTimeout(detectAndSave, 3000);" +
        "})();";

    /**
     * 生成账号密码自动填充脚本
     *
     * @param username 账号
     * @param password 密码
     * @return 填充用的 JS 字符串
     */
    public static String getFillScript(String username, String password) {
        String escapedUser = escape(username);
        String escapedPass = escape(password);
        
        return "(function() {" +
               "    console.log('Manor: Starting autofill');" +
               "    " +
               "    var passInputs = document.querySelectorAll('input[type=\"password\"]');" +
               "    // 备选方案：通过 id 或 name 模糊匹配密码框" +
               "    if (passInputs.length === 0) {" +
               "        passInputs = document.querySelectorAll('input[name*=\"pass\"], input[id*=\"pass\"]');" +
               "    }" +
               "    " +
               "    if (passInputs.length > 0) {" +
               "        var passInput = passInputs[0];" +
               "        passInput.value = \"" + escapedPass + "\";" +
               "        " +
               "        var form = passInput.form || passInput.closest('form');" +
               "        if (!form) {" +
               "            form = passInput.parentElement;" +
               "            while (form && form.tagName !== 'FORM' && form !== document.body) {" +
               "                form = form.parentElement;" +
               "            }" +
               "        }" +
               "        " +
               "        if (form) {" +
               "            var userInput = null;" +
               "            var inputs = Array.from(form.querySelectorAll('input'));" +
               "            " +
               "            // 遍历并寻找最符合账号特征的 input" +
               "            for (var i = 0; i < inputs.length; i++) {" +
               "                var inp = inputs[i];" +
               "                if (inp === passInput) break;" +
               "                " +
               "                var type = inp.type.toLowerCase();" +
               "                var name = (inp.name || '').toLowerCase();" +
               "                var id = (inp.id || '').toLowerCase();" +
               "                var placeholder = (inp.placeholder || '').toLowerCase();" +
               "                " +
               "                if (type === 'text' || type === 'email' || type === 'tel' || " +
               "                    name.includes('user') || name.includes('email') || name.includes('login') || name.includes('account') ||" +
               "                    id.includes('user') || id.includes('email') || id.includes('login') || id.includes('account') ||" +
               "                    placeholder.includes('user') || placeholder.includes('email') || placeholder.includes('login')) {" +
               "                    userInput = inp;" +
               "                }" +
               "            }" +
               "            " +
               "            if (userInput) {" +
               "                userInput.value = \"" + escapedUser + "\";" +
               "                // 手动触发 input 和 change 事件，确保网页 JS 能够捕捉到值的变化" +
               "                userInput.dispatchEvent(new Event('input', { bubbles: true }));" +
               "                userInput.dispatchEvent(new Event('change', { bubbles: true }));" +
               "                console.log('Manor: Username filled');" +
               "            }" +
               "        }" +
               "        " +
               "        passInput.dispatchEvent(new Event('input', { bubbles: true }));" +
               "        passInput.dispatchEvent(new Event('change', { bubbles: true }));" +
               "        console.log('Manor: Password filled');" +
               "    } else {" +
               "        console.log('Manor: No password input found');" +
               "    }" +
               "})();";
    }

    /**
     * 对 Java 字符串进行转义，防止破坏注入的 JS 引号结构。
     */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
