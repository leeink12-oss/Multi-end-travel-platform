<template>
  <div class="support-wrapper">
    <el-card class="support-card" :body-style="{ padding: '0' }">
      <div class="support-hero">
        <div>
          <div class="page-kicker">平台客服 / Support</div>
          <div class="title">智能行程助手</div>
          <div class="sub">输入目的地、酒店、景点或餐饮相关问题，快速获取推荐与跳转链接。</div>
        </div>
        <div class="support-badge">在线服务</div>
      </div>

      <div class="chat-shell">
        <div class="quick-tags">
          <el-tag effect="light" @click="fillQuestion('推荐北京性价比高的酒店')">酒店推荐</el-tag>
          <el-tag effect="light" @click="fillQuestion('帮我推荐杭州周边景点')">景点推荐</el-tag>
          <el-tag effect="light" @click="fillQuestion('推荐适合家庭聚餐的餐厅')">餐饮推荐</el-tag>
          <el-tag effect="light" @click="fillQuestion('帮我规划上海到广州的出行')">出行规划</el-tag>
        </div>

        <div class="chat-window" ref="chatWindowRef">
          <div
            v-for="(m, idx) in messages"
            :key="idx"
            class="msg-row"
            :class="m.role === 'user' ? 'from-user' : 'from-bot'"
          >
            <div class="msg-bubble">
              <div class="msg-role">
                {{ m.role === 'user' ? '我' : '智能行程助手' }}
              </div>
              <div class="msg-text" v-if="m.thinking">
                {{ m.content }}
              </div>
              <div class="msg-text msg-text-with-links" v-else-if="m.role === 'assistant'">
                <template v-for="(seg, i) in parseContent(m.content)" :key="i">
                  <span v-if="seg.type === 'text'">{{ seg.value }}</span>
                  <router-link v-else-if="seg.type === 'hotel'" :to="'/hotel/' + seg.id" class="assistant-link">前往预订</router-link>
                  <router-link v-else-if="seg.type === 'scenic'" :to="'/scenic/' + seg.id" class="assistant-link">前往预订</router-link>
                  <router-link v-else-if="seg.type === 'restaurant'" :to="'/dining/' + seg.id" class="assistant-link">前往预订</router-link>
                </template>
              </div>
              <div class="msg-text" v-else>
                {{ m.content }}
              </div>
            </div>
          </div>
        </div>

        <div class="input-area">
          <el-input
            v-model="question"
            type="textarea"
            :rows="3"
            resize="none"
            placeholder="请输入你想咨询的问题，例如：推荐北京的酒店、某地有什么景点？"
            @keyup.enter.exact.prevent="handleSend"
          />
          <div class="actions">
            <el-button type="primary" :loading="loading" class="send-btn" @click="handleSend">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const question = ref('');
const loading = ref(false);
const messages = ref([]);
const chatWindowRef = ref(null);

const scrollToBottom = async () => {
  await nextTick();
  if (chatWindowRef.value) {
    chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight;
  }
};

const fillQuestion = (text) => {
  question.value = text;
};

/** 解析回复中的 [酒店:ID] [景点:ID] [餐饮:ID]，用于渲染为跳转链接 */
function parseContent(text) {
  if (!text || typeof text !== 'string') return [{ type: 'text', value: '' }];
  const segments = [];
  // 约定格式：后端提示模型输出 [酒店:ID] / [景点:ID] / [餐饮:ID]，用于在聊天里生成可跳转的下单链接。
  // 只识别数字 ID，避免误把普通文本当链接。
  const re = /\[(酒店|景点|餐饮):(\d+)\]/g;
  let lastIndex = 0;
  let m;
  while ((m = re.exec(text)) !== null) {
    if (m.index > lastIndex) {
      segments.push({ type: 'text', value: text.slice(lastIndex, m.index) });
    }
    segments.push({
      type: m[1] === '酒店' ? 'hotel' : m[1] === '景点' ? 'scenic' : 'restaurant',
      id: parseInt(m[2], 10)
    });
    lastIndex = re.lastIndex;
  }
  if (lastIndex < text.length) {
    segments.push({ type: 'text', value: text.slice(lastIndex) });
  }
  return segments.length ? segments : [{ type: 'text', value: text }];
}

const handleSend = async () => {
  if (!question.value.trim()) {
    ElMessage.warning('请输入问题');
    return;
  }
  const content = question.value.trim();
  messages.value.push({ role: 'user', content });
  question.value = '';
  loading.value = true;
  // UX：请求模型接口通常有延迟，这里先插入“思考中”气泡，返回后再替换为真实回复
  messages.value.push({
    role: 'assistant',
    content: '智能行程助手思考中...',
    thinking: true
  });
  scrollToBottom();
  try {
    const res = await api.post('/support/ask', {
      question: content
    });
    // 移除思考中占位
    messages.value = messages.value.filter((msg) => !msg.thinking);
    if (res.data.code === 0) {
      messages.value.push({
        role: 'assistant',
        content: res.data.data || '（暂无回复）'
      });
    } else {
      ElMessage.error(res.data.message || '智能行程助手暂时不可用');
    }
  } catch (e) {
    messages.value = messages.value.filter((msg) => !msg.thinking);
    ElMessage.error('调用智能行程助手失败，请稍后再试');
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};
</script>

<style scoped>
.support-wrapper {
  width: 70%;
  max-width: 980px;
  margin: 24px auto 40px;
}
.support-card {
  border: none;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
}
.support-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 22px 24px;
  background: linear-gradient(135deg, #f7fbff 0%, #eef6ff 100%);
  border-bottom: 1px solid #e4eef9;
}
.page-kicker {
  font-size: 12px;
  color: #7a8798;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  margin-bottom: 6px;
}
.title {
  font-size: 28px;
  font-weight: 800;
  color: #1a1a1a;
}
.sub {
  margin-top: 6px;
  color: #667085;
  font-size: 13px;
  line-height: 1.7;
}
.support-badge {
  padding: 8px 14px;
  border-radius: 999px;
  background: #1e73be;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}
.chat-shell {
  padding: 18px 20px 20px;
}
.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}
.quick-tags :deep(.el-tag) {
  cursor: pointer;
  border-radius: 999px;
  padding: 0 12px;
  height: 32px;
}
.chat-window {
  border: 1px solid #eef3f8;
  border-radius: 16px;
  padding: 16px;
  height: 420px;
  overflow-y: auto;
  background: linear-gradient(180deg, #fbfdff 0%, #ffffff 100%);
}
.msg-row {
  display: flex;
  margin-bottom: 12px;
}
.msg-row.from-user {
  justify-content: flex-end;
}
.msg-row.from-bot {
  justify-content: flex-start;
}
.msg-bubble {
  max-width: 78%;
  padding: 10px 12px;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  border: 1px solid #eef3f8;
}
.from-user .msg-bubble {
  background: linear-gradient(135deg, #1e73be 0%, #2f8de4 100%);
  color: #fff;
  border-color: transparent;
}
.msg-role {
  font-size: 12px;
  margin-bottom: 4px;
  opacity: 0.9;
  font-weight: 700;
}
.msg-text {
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
}
.msg-text-with-links {
  line-height: 1.7;
}
.assistant-link {
  color: #1e73be;
  text-decoration: none;
  margin: 0 2px;
  font-weight: 700;
}
.assistant-link:hover {
  text-decoration: underline;
}
.from-user .assistant-link {
  color: #fff;
  text-decoration: underline;
}
.input-area {
  margin-top: 16px;
  border: 1px solid #eef3f8;
  border-radius: 16px;
  padding: 14px;
  background: #fff;
}
.actions {
  text-align: right;
  margin-top: 10px;
}
.send-btn {
  min-width: 100px;
  border-radius: 10px;
}
@media (max-width: 1100px) {
  .support-wrapper {
    width: 92%;
  }
  .support-hero {
    flex-direction: column;
    align-items: flex-start;
  }
  .chat-window {
    height: 380px;
  }
}
</style>

