<script setup>
import {Check, Document} from "@element-plus/icons-vue";
import {computed,onMounted,reactive,ref} from "vue";
import {getTopictypeService,submitTopic} from "@/api/topicApi.js";
import {useUserStore} from "@/store/userStore.js";
import {Delta, Quill, QuillEditor} from "@vueup/vue-quill";
import ImageResize from "quill-image-resize-vue";
import { ImageExtend, QuillWatch } from "quill-image-super-solution-module";
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import {ElMessage} from "element-plus";
import ColorDot from "@/components/colorDot.vue";
const ustore=useUserStore()
const props=defineProps({
  show: Boolean,
  defaultTitle: {
    default: '',
    type: String
  },
  defaultText: {
    default: '',
    type: String
  },
  defaultType: {
    default: null,
    type: Number
  },
  submitButton: {
    default: '立即发表主题',
    type: String
  },
  submit: {
    default: async (editor,close) => {
      await submitTopic(editor.type.id,editor.title,editor.text)
      ElMessage.success('发贴成功!')
      //this.$emit('close');
      close()
    },
    type: Function
  }

})
//字传父
const emit=defineEmits(['close','success'])
const editor=reactive({
  type: null,
  title: '',
  text: '',
})
const refEditor = ref()
function initEditor() {
  if(props.defaultText)
    editor.text = new Delta(JSON.parse(props.defaultText))
  else
    refEditor.value.setContents('', 'user')
    editor.title = props.defaultTitle
    editor.type=findTypeById(props.defaultType)
}
//通过id查找类型
function findTypeById(id){
  for (let type of ustore.forum.types) {
    if(type.id === id)
      return type
  }
}
//统计text文字数量
function deltaToText(delta){
  if(!delta.ops) return ""
  let str=""
  for(let op of delta.ops)
    str +=op.insert
  return str.replace(/\s/g,"")
}
//提交前进行判断
function postTopic() {
  const text = deltaToText(editor.text)
  if(text.length > 20000) {
    ElMessage.warning('字数超出限制，无法发布主题！')
    return
  }
  if(!editor.title) {
    ElMessage.warning('请填写标题！')
    return
  }
  if(!editor.type) {
    ElMessage.warning('请选择一个合适的帖子类型！')
    return
  }
  if(!editor) {
    ElMessage.warning('请输入框不能为空')
    return
  }
  //submit()
  props.submit(editor,()=>emit('close'))


 // props.submit(editor, () => emit('success'))
}
const textlength=computed(()=>deltaToText(editor.text).length)

//发送帖子
// const submit=async ()=>{
//   await submitTopic(editor.type.id,editor.title,editor.text)
//     ElMessage.success('发贴成功!')
//     emit('close')
// }
Quill.register('modules/imageResize', ImageResize)
Quill.register('modules/ImageExtend', ImageExtend)
const editorOption= {
  modules: {
    toolbar: {
      container: [
        "bold", "italic", "underline", "strike","clean",
        {color: []}, {'background': []},
        {size: ["small", false, "large", "huge"]},
        { header: [1, 2, 3, 4, 5, 6, false] },
        {list: "ordered"}, {list: "bullet"}, {align: []},
        "blockquote", "code-block", "link", "image",
        { indent: '-1' }, { indent: '+1' }
      ],
      handlers: {
        'image': function () {
          QuillWatch.emit(this.quill.id)
        }
      }
    },
    imageResize: {
      modules: [ 'Resize', 'DisplaySize' ]
    },
    ImageExtend: {
      action:   'http://localhost:8080/api/image/cache',
      name: 'file',
      size: 5,
      loading: true,
      accept: 'image/png, image/jpeg',
      response: (resp) => {
        if(resp.data) {
          //显示在文本框内
          return  'http://localhost:8080/image' + resp.data
        } else {
          return null
        }
      },
      methods: 'POST',
      headers: xhr => {
        xhr.setRequestHeader('Authorization','Bearer ' +ustore.getToken());
      },
      start: () => editor.uploading = true,
      success: () => {
        ElMessage.success('图片上传成功!')
        editor.uploading = false
      },
      error: () => {
        ElMessage.warning('图片上传失败，请联系管理员!')
        editor.uploading = false
      }
    }
  }
}

onMounted(()=>{
})
</script>

<template>
    <div>
      <el-drawer
          @open="initEditor"
          :model-value="show"
          direction="btt"
          :close-on-click-modal="false"
          :size="650"
          @close="emit('close')">
        <template #header>
          <div>
            <div style="font-weight: bold">发表新帖子</div>
            <div style="font-size: 13px">发表帖子前，请遵守相关法律法规。</div>
          </div>
        </template>
        <div style="display: flex;gap: 10px">
          <div style="width: 150px">
<!--  :value="item" 是用来指定 <el-option> 组件中每个选项的实际值的。     当用户选择了某个选项时，editor.type 的值将被设置为该选项对象 item。     -->
            <el-select placeholder="选择主体类型..." value-key="id" v-model="editor.type" >
              <el-option v-for="item in ustore.forum.types.filter(type=>type.id>0)" :value="item" :label="item.name">
                <div>
                  <color-dot :color="item.color"></color-dot>
                  <span style="margin-left: 10px">{{item.name}}</span>
                </div>
              </el-option>
            </el-select>
          </div>
          <div style="flex: 1">
            <el-input v-model="editor.title"  placeholder="请输入帖子标题..." :prefix-icon="Document" style="height: 100%" maxlength="30"></el-input>
          </div>
        </div>
        <div style="margin-top: 10px;font-size: 13px;color: dodgerblue">
          <div v-if="editor.type">
            <color-dot :color="editor.type.color"></color-dot>
            {{ editor.type.desc }}
          </div>
          <div v-else>
            <!-- 当editor.type不存在时，显示提示信息 -->
            <span style="color: dodgerblue">请选择帖子类型</span>
          </div>
        </div>
        <div style="margin-top: 15px;height: 440px;overflow: hidden" v-loading="editor.uploading" element-loading-text="正在上传图片，请稍后...">
          <quill-editor v-model:content="editor.text" style="height: calc(100% - 45px) "
                        ref="refEditor"
                        content-type="delta"
                        placeholder="今天想分享点什么呢？" :options="editorOption"></quill-editor>
        </div>
        <div style="display: flex;justify-content: space-between;margin-top: 5px">
          <div style="font-size: 13px;color: grey">
            当前字数 {{textlength}} （最大支持20000字）
          </div>
          <div>
            <el-button type="success" @click="postTopic" :icon="Check"  plain>{{ submitButton }}</el-button>
          </div>
        </div>
      </el-drawer>
    </div>
</template>

<style scoped lang="scss">
//样式穿刺
:deep(.el-drawer){
  width: 800px;
  margin: auto;
  border-radius: 10px 10px 0 0;
}
:deep(.el-drawer__header){
  margin: 0;
}
:deep(.ql-toolbar){
  border-radius: 5px 5px 0 0;
}
:deep(.ql-container){
  border-radius: 5px 5px 0 0;
}
:deep(.ql-editor){
  font-size: 14px;
}
</style>