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
using AVOSCloud.Internal;
using System.Threading;
using System.IO;
using Microsoft.Phone.Tasks;

namespace AVOSCloud.WP8.Demo
{
    public partial class MainPage : PhoneApplicationPage
    {
        // Constructor
        public MainPage()
        {
            InitializeComponent();

            // Set the data context of the listbox control to the sample data
            DataContext = App.ViewModel;

            // Sample code to localize the ApplicationBar
            //BuildLocalizedApplicationBar();
        }

        // Load data for the ViewModel Items
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            if (!App.ViewModel.IsDataLoaded)
            {
                App.ViewModel.LoadData();
            }
        }

        private async void Button_Click(object sender, RoutedEventArgs e)
        {
            #region 数据的存储是AVOS Cloud 提供的最基础的服务，它在客户端只需要如下简单的操作就可以。

            AVObject player = new AVObject("Player");//表的名字为：Player，在AVOS Cloud 网站上控制台里面可以查看表的信息。web：https://cn.avoscloud.com/applist.html#/apps 需要登录。
            //AVOS Cloud 支持最常用的几种数据类型，详细的请查看：https://cn.avoscloud.com/docs/data_security.html#数据
            player["name"] = txb_playerName.Text.Trim();//字符串
            player["age"] = int.Parse(txb_age.Text.Trim());//整型
            player["isAdult"] = int.Parse(txb_age.Text.Trim()) >= 18 ? true : false;//bool 布尔类型
            Task saveTask = player.SaveAsync();//
            await saveTask;//采用.NET 4.5之后推荐的异步保存到服务端。

            #endregion
        }

        private async void Button_Click_1(object sender, RoutedEventArgs e)
        {
            var gameScore = new AVObject("GameScore")
            {
            { "score", 1338 },
            { "playerName", "Peter Burke" },
            { "cheatMode", false },
            { "skills", new List<string> { "FBI", "Agent Leader" } },
            };
            await gameScore.SaveAsync().ContinueWith(t =>
            {
                // 保存成功之后，修改一个已经在服务端生效的数据，这里我们修改cheatMode和score
                // AVOSCloud只会针对指定的属性进行覆盖操作，本例中的playerName不会被修改
                gameScore["cheatMode"] = true;
                gameScore["score"] = 9999;
                return gameScore.SaveAsync();
            });
        }

        private async void Button_Click_2(object sender, RoutedEventArgs e)
        {
            byte[] data = System.Text.Encoding.UTF8.GetBytes(txb_word.Text.Trim());
            AVFile file = new AVFile("mytxtFile.txt", data, new Dictionary<string, object>()
            {
            {"author","AVOSCloud"}
            });
            await file.SaveAsync().ContinueWith(
                t =>
                {
                    if (!t.IsFaulted)
                    {
                        MessageBox.Show(file.ObjectId);
                    }
                    else
                    {

                    }
                }, CancellationToken.None, TaskContinuationOptions.OnlyOnRanToCompletion, TaskScheduler.FromCurrentSynchronizationContext());
            #region 以下代码可以实现将本地文件上传到AVOS Cloud 的服务端，进行保存,并且可以跟踪上传进度。
            AVFile localFile = AVFile.CreateFileWithLocalPath("screenshot.PNG", Path.Combine("<Local Folder Path>", "screenshot.PNG"));
            await localFile.SaveAsync(new Progress<AVUploadProgressEventArgs>(p =>
            {
                var progress = p.Progress;
            }));
            #endregion
        }

        private void btn_sendSMSCode_Click(object sender, RoutedEventArgs e)
        {
            PopupMsg(AVCloud.RequestSMSCode(txb_phone.Text.Trim()),"发送验证码到 "+txb_phone.Text.Trim()+" 成功。");
        }

        private void Button_Click_3(object sender, RoutedEventArgs e)
        {
            AVCloud.RequestSMSCode(txb_phone.Text.Trim(), txb_appName.Text.Trim(), txb_operationName.Text.Trim(), int.Parse(txb_ttl.Text.Trim()));
        }

        private void btn_Verify_Click(object sender, RoutedEventArgs e)
        {
            AVCloud.VerifySmsCode(txb_smsCode.Text.Trim());
        }

        private void btn_goToRelationTutorial_Click(object sender, RoutedEventArgs e)
        {
            NavigationService.Navigate(new Uri("/RelationTutorial.xaml", UriKind.RelativeOrAbsolute));
        }

        private async void Button_Click_4(object sender, RoutedEventArgs e)
        {
            string filePath = Path.Combine("Shared", "Media", "GuoJiGe.mp3");
            AVFile file = AVFile.CreateFileWithLocalPath("GuoJiGe.mp3", filePath);
            await file.SaveAsync();
        }
        PhotoChooserTask photoChooserTask;
        private void Button_Click_5(object sender, RoutedEventArgs e)
        {
            photoChooserTask = new PhotoChooserTask();
            photoChooserTask.Completed += new EventHandler<PhotoResult>(photoChooserTask_Completed);
            photoChooserTask.Show();
        }
        void photoChooserTask_Completed(object sender, PhotoResult e)
        {
            if (e.TaskResult == TaskResult.OK)
            {
                //MessageBox.Show(e.ChosenPhoto.Length.ToString());
                AVFile file = new AVFile("WPSelected.png", e.ChosenPhoto);
                file.SaveAsync().ContinueWith(
                t =>
                {
                    if (!t.IsFaulted)
                    {
                        MessageBox.Show(file.ObjectId);
                    }
                    else
                    {

                    }
                }, CancellationToken.None, TaskContinuationOptions.OnlyOnRanToCompletion, TaskScheduler.FromCurrentSynchronizationContext());
                //Code to display the photo on the page in an image control named myImage.
                //System.Windows.Media.Imaging.BitmapImage bmp = new System.Windows.Media.Imaging.BitmapImage();
                //bmp.SetSource(e.ChosenPhoto);
                //myImage.Source = bmp;
            }
        }

        void PopupMsg(Task task, string msg)
        {
            task.ContinueWith(
                t =>
                {
                    if (!t.IsFaulted)
                    {
                        MessageBox.Show(msg);
                    }
                    else
                    {

                    }
                }, CancellationToken.None, TaskContinuationOptions.OnlyOnRanToCompletion, TaskScheduler.FromCurrentSynchronizationContext());
        }
        // Sample code for building a localized ApplicationBar
        //private void BuildLocalizedApplicationBar()
        //{
        //    // Set the page's ApplicationBar to a new instance of ApplicationBar.
        //    ApplicationBar = new ApplicationBar();

        //    // Create a new button and set the text value to the localized string from AppResources.
        //    ApplicationBarIconButton appBarButton = new ApplicationBarIconButton(new Uri("/Assets/AppBar/appbar.add.rest.png", UriKind.Relative));
        //    appBarButton.Text = AppResources.AppBarButtonText;
        //    ApplicationBar.Buttons.Add(appBarButton);

        //    // Create a new menu item with the localized string from AppResources.
        //    ApplicationBarMenuItem appBarMenuItem = new ApplicationBarMenuItem(AppResources.AppBarMenuItemText);
        //    ApplicationBar.MenuItems.Add(appBarMenuItem);
        //}
    }
}