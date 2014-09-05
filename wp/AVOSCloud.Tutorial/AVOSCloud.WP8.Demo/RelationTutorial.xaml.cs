using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using System.Threading.Tasks;

namespace AVOSCloud.WP8.Demo
{
    public partial class RelationTutorial : PhoneApplicationPage
    {
        public RelationTutorial()
        {
            InitializeComponent();
        }

        private async void Button_Click(object sender, RoutedEventArgs e)
        {
            AVObject girlType = new AVObject("GirlType");
            girlType["typeName"] = "Hot";
            AVObject beckham = new AVObject("Boy");
            beckham["name"] = "AVOS Cloud User 1";
            beckham["age"] = 38;
            beckham["focusType"] = girlType;//这样使得在AVOS Cloud 的控制台Web上显示的就是：Boy的字段focusType 是一个 指向 GirlType 的Pointer的类型。Pointer 可以理解为一个指向AVObject 指针。
            await beckham.SaveAsync();//保存beckham的时候会自动将girlType也保存到服务器。
        }

        private async void Button_Click_1(object sender, RoutedEventArgs e)
        {
            AVQuery<AVObject> findGirlType = new AVQuery<AVObject>("GirlType").WhereEqualTo("typeName", "Hot");
            await findGirlType.FindAsync().ContinueWith(t => 
            {
                var hot = t.Result.First();//可能有多个标记为Hot的GirType，但是“弱水三千只取一瓢”。
                var myself = new AVObject("Boy");
                myself["name"] = "AVOS Cloud User 2";
                myself["age"] = 18;
                myself["focusType"] = hot;//关联被查询出来的对象。
                return myself.SaveAsync();
            });
        }

        private async void Button_Click_2(object sender, RoutedEventArgs e)
        {
            #region 一对多的关系，在保存在服务端之前，所有对象必须已经存在于服务端。
            AVObject weibo = new AVObject("Weibo");
            weibo["content"] = "AVOS Cloud 什么的最萌了！";
            //weibo["user"] = new AVUser() { Username = "王思聪", Password = "showmemoney" };
            //王校长的用户名已被别人抢注了，所以要正确关联用户，请开发者自定义个名字。

            AVObject comment1 = new AVObject("Comment");
            comment1["words"] = "第一条，好鸡冻！";

            AVObject comment2 = new AVObject("Comment");
            comment2["words"] = "点赞必回粉！";

            AVObject comment3 = new AVObject("Comment");
            comment3["words"] = "优质新西兰奶粉代购，请加q12345789";

            AVObject comment4 = new AVObject("Comment");
            comment4["words"] = "5元千粉，需要的请联系138XXXX";
            await Task.WhenAll(new Task[] { weibo.SaveAsync(), comment1.SaveAsync(), comment2.SaveAsync(), comment3.SaveAsync(), comment4.SaveAsync() }).ContinueWith(t =>
            {
                AVRelation<AVObject> weiboRelation = weibo.GetRelation<AVObject>("comments");
                weiboRelation.Add(comment1);
                weiboRelation.Add(comment2);
                weiboRelation.Add(comment3);
                weiboRelation.Add(comment4);
                return weibo.SaveAsync();
            });
            #endregion

        }
    }
}