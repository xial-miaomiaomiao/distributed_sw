const API_BASE = '/api';

function getProductId() {
    return document.getElementById('productId').value;
}

function showResult(content, isError = false) {
    const resultBox = document.getElementById('result');
    if (isError) {
        resultBox.innerHTML = `<p class="error">❌ ${content}</p>`;
    } else {
        resultBox.innerHTML = `<pre>${content}</pre>`;
    }
}

function showLoading() {
    document.getElementById('result').innerHTML = '<p class="loading">⏳ 请求中...</p>';
}

async function fetchProduct(url, strategy) {
    showLoading();
    const startTime = Date.now();
    
    try {
        const response = await fetch(url);
        const data = await response.json();
        const endTime = Date.now();
        
        const result = {
            策略: strategy,
            响应时间: `${endTime - startTime}ms`,
            处理服务端口: data.serverPort,
            查询成功: data.success,
            数据: data.data,
            原始响应: data
        };
        
        showResult(JSON.stringify(result, null, 2));
    } catch (error) {
        showResult(`请求失败: ${error.message}`, true);
    }
}

function testBasic() {
    const id = getProductId();
    fetchProduct(`${API_BASE}/products/${id}`, '基础查询');
}

function testPenetration() {
    const id = getProductId();
    fetchProduct(`${API_BASE}/products/penetration/${id}`, '防止缓存穿透');
}

function testBreakdown() {
    const id = getProductId();
    fetchProduct(`${API_BASE}/products/breakdown/${id}`, '防止缓存击穿');
}

function testAvalanche() {
    const id = getProductId();
    fetchProduct(`${API_BASE}/products/avalanche/${id}`, '防止缓存雪崩');
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 高并发读测试系统已加载');
    
    // 测试Nginx是否正常
    fetch('/nginx-health')
        .then(response => response.text())
        .then(text => console.log('Nginx状态:', text))
        .catch(err => console.log('Nginx连接测试:', err.message));
});
