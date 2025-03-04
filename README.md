# 語我聊療<畢業專題>
> Student Graduation Project
---
## 介紹
語我聊療是一款與聯合醫院語言治療師合作，協助失語症病患在沒有語言治療師陪同下，提升自我學習頻率與效率，針對不同的嚴重程度對患者制定不同的復健方式，改變語言復健的頻率和靈活性，幫助他們恢復及改善語言表達能力。


---
## 功能
[功能介紹影片](https://youtu.be/bwDh1vAfi1s)

### 使用者端(病患、病患家人)
- 基本資料
  根據八大失語症類型，選擇適合的題目難度
![專題PPT pptx](https://github.com/user-attachments/assets/49d9b45f-c322-40a4-9046-c3c2696ca82d)

- 三大內容
  1. 領域選擇：讓患者選擇有興趣的領域，提升使用意願
     ![專題PPT pptx (1)](https://github.com/user-attachments/assets/11517fc8-e57d-45f6-b123-717dc6777afd)
  3. 加強練習：根據語言治療師實際訓練的三大方面(流暢、理解、重述)
      ![專題PPT pptx (2)](https://github.com/user-attachments/assets/ca084b80-379e-47fe-8b22-1ff6f5aecd63)

  5. 個人資料：紀錄病患作答歷史資料
      ![專題PPT pptx (3)](https://github.com/user-attachments/assets/37f7f073-6594-4f61-a968-08cc9d93f236)
![專題PPT pptx (4)](https://github.com/user-attachments/assets/4bb277bb-bb08-45eb-8317-e3df0c150f5a)

- 六大題型
![Lucy's 履歷 (2)](https://github.com/user-attachments/assets/411ac991-923c-43dd-b5bf-242f5245b4aa)

  1. 簡單應答：根據圖片說出內容物
  2. 複誦句子：念出題目中的句子
  3. 聽覺理解：根據題目敘述點選正確的圖片
  4. 圖物配對：將正確答案移至對應照片的空格內
  5. 口語描述：描述圖片中的內容
  6. 詞語表達：根據圖片點選正確的句子描述
- 語音處理
  1. 前端錄音並存取音檔
  2. 後端將音檔降躁、轉檔再語音轉文字
     [後端專案](https://github.com/LUCY0299/voice2txt.git)
  4. 最後將後端串接至資料庫，提供管理者頁面之資料提取
### 管理者端(治療師)
- 網頁內容
  1. 提供患者每次作答紀錄(日期時間、練習時長、題目類型)
  2. 音檔與音檔之文字
---
## 與聯合醫院至療師合作紀錄
![image](https://github.com/user-attachments/assets/1f262a99-9c4b-476c-ae80-4c3752afb724)


---
## Technologies Used
- Database: Google Firebase
- Backend Software: Python
- Frontend Software: Kotlin, Android Studio, Amazon Lightsail
- Version Control: GitHub
---
## Acknowledgments
致謝於專題成員以及專題教授

- @JennyChang1124
- @WeiJia1022
- @Kon245
- CHEN,CHIA-JUNG
- LIN, TZU-TING
