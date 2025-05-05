repeat task.wait() until game:IsLoaded()

-- [1] SERVICE & BIẾN TOÀN CỤC
local Players = game:GetService("Players")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Workspace = game:GetService("Workspace")
local RunService = game:GetService("RunService")
local TeleportService = game:GetService("TeleportService")
local TweenService = game:GetService("TweenService")
local HttpService = game:GetService("HttpService")
local Lighting = game:GetService("Lighting")
local VirtualInputManager = game:GetService("VirtualInputManager")

local LP = Players.LocalPlayer
local Mouse = LP:GetMouse()
local Camera = Workspace.CurrentCamera

getgenv().Settings = {
    AutoBounty = false,
    UseZ = true,
    UseX = true,
    UseC = true,
    UseF = true,
    SafeRange = 100,
    AutoHop = true,
    SelectWeapon = "Cursed Dual Katana",
    KillCount = 0,
    EspPvP = true,
    KillEffect = true,
    AutoHaki = true,
    AutoObservation = true,
    AutoDodge = true,
    AvoidSkillRange = 35,
    FPSBoost = true
}

-- [2] UI LOADER (KAVO STYLE)
local Library = loadstring(game:HttpGet("https://raw.githubusercontent.com/xHeptc/Kavo-UI-Library/main/source.lua"))()
local Window = Library.CreateLib("GiaHuy Hub | Full Auto Bounty", "BloodTheme")

local MainTab = Window:NewTab("Auto Bounty")
local SkillTab = Window:NewTab("Chiêu Thức")
local SettingTab = Window:NewTab("Cài Đặt")
local VisualTab = Window:NewTab("Hiệu Ứng")

-- [3] TAB AUTO BOUNTY
local main = MainTab:NewSection("Tự động săn bounty")
main:NewToggle("Bật Auto Bounty", "Đánh người bật PvP", function(v)
    Settings.AutoBounty = v
end)
main:NewDropdown("Chọn Vũ Khí", "Ưu tiên vũ khí này", {
    "Cursed Dual Katana", "Dragon Talon", "Electric Claw", "Sharkman Karate", "Dough", "Superhuman"
}, function(v)
    Settings.SelectWeapon = v
end)
main:NewLabel("Kill Count: 0")

-- [4] TAB KỸ NĂNG
local skill = SkillTab:NewSection("Chiêu thức")
skill:NewToggle("Dùng Z", "", function(v) Settings.UseZ = v end)
skill:NewToggle("Dùng X", "", function(v) Settings.UseX = v end)
skill:NewToggle("Dùng C", "", function(v) Settings.UseC = v end)
skill:NewToggle("Dùng F", "", function(v) Settings.UseF = v end)

-- [5] TAB CÀI ĐẶT
local set = SettingTab:NewSection("Tùy chỉnh nâng cao")
set:NewToggle("Auto Hop khi hết mục tiêu", "", function(v) Settings.AutoHop = v end)
set:NewSlider("Khoảng cách level", "Không đánh người trên +X lv", 500, 50, function(v)
    Settings.SafeRange = v
end)

set:NewToggle("Auto bật Haki", "", function(v) Settings.AutoHaki = v end)
set:NewToggle("Auto bật Quan sát", "", function(v) Settings.AutoObservation = v end)
set:NewToggle("Auto né kỹ năng", "", function(v) Settings.AutoDodge = v end)

-- [6] TAB HIỆU ỨNG
local vis = VisualTab:NewSection("ESP & Kill Effect")
vis:NewToggle("ESP PvP", "Highlight người bật PvP", function(v)
    Settings.EspPvP = v
end)
vis:NewToggle("Hiệu ứng Kill (Nổ + Rung)", "", function(v)
    Settings.KillEffect = v
end)
vis:NewToggle("Tăng FPS", "Tối ưu lag", function(v)
    Settings.FPSBoost = v
    if v then
        for _, v in pairs(Lighting:GetChildren()) do
            if v:IsA("PostEffect") then v.Enabled = false end
        end
        sethiddenproperty(Lighting, "Technology", Enum.Technology.Compatibility)
        setfpscap(60)
    end
end)

-- [7] CHỐNG AFK
spawn(function()
    while true do
        VirtualInputManager:SendKeyEvent(true, "W", false, game)
        wait(60)
    end
end)

-- [8] AUTO BOUNTY CORE LOOP
function GetTarget()
    local closest = nil
    local minDist = math.huge
    for _, player in pairs(Players:GetPlayers()) do
        if player ~= LP and player.Character and player.Character:FindFirstChild("HumanoidRootPart") then
            local hrp = player.Character.HumanoidRootPart
            local hum = player.Character:FindFirstChild("Humanoid")
            local level = player:FindFirstChild("Data") and player.Data.Level.Value or 9999
            local isPvP = player.Character:FindFirstChild("ForceField") == nil
            if isPvP and hum and hum.Health > 0 and level <= LP.Data.Level.Value + Settings.SafeRange then
                local dist = (LP.Character.HumanoidRootPart.Position - hrp.Position).Magnitude
                if dist < minDist then
                    minDist = dist
                    closest = player
                end
            end
        end
    end
    return closest
end

function EquipTool(name)
    for _,v in pairs(LP.Backpack:GetChildren()) do
        if v:IsA("Tool") and v.Name:find(name) then
            LP.Character.Humanoid:EquipTool(v)
        end
    end
end

function UseSkill(key)
    keypress(key) wait(0.2) keyrelease(key)
end

function AttackTarget(target)
    local myHRP = LP.Character:FindFirstChild("HumanoidRootPart")
    local targetHRP = target.Character:FindFirstChild("HumanoidRootPart")
    if myHRP and targetHRP then
        repeat
            pcall(function()
                EquipTool(Settings.SelectWeapon)
                myHRP.CFrame = targetHRP.CFrame * CFrame.new(0, 0, 3)
                if Settings.UseZ then UseSkill(0x5A) end
                if Settings.UseX then UseSkill(0x58) end
                if Settings.UseC then UseSkill(0x43) end
                if Settings.UseF then UseSkill(0x46) end
            end)
            wait()
        until not target or not target.Character or target.Character:FindFirstChild("Humanoid").Health <= 0 or not Settings.AutoBounty
        Settings.KillCount += 1
    end
end

-- [9] LOOP CHÍNH
spawn(function()
    while task.wait(1) do
        if Settings.AutoBounty then
            local target = GetTarget()
            if target then
                AttackTarget(target)
            elseif Settings.AutoHop then
                TeleportService:Teleport(game.PlaceId)
            end
        end
    end
end)
-- [10] MODULE ESP PVP
function CreateESP(player)
    if not player.Character or not player.Character:FindFirstChild("HumanoidRootPart") then return end
    local billboard = Instance.new("BillboardGui", player.Character)
    billboard.Name = "PvP_ESP"
    billboard.Size = UDim2.new(0, 100, 0, 40)
    billboard.StudsOffset = Vector3.new(0, 4, 0)
    billboard.Adornee = player.Character:WaitForChild("HumanoidRootPart")
    billboard.AlwaysOnTop = true

    local label = Instance.new("TextLabel", billboard)
    label.Size = UDim2.new(1, 0, 1, 0)
    label.BackgroundTransparency = 1
    label.Text = "PvP: " .. player.Name
    label.TextColor3 = Color3.fromRGB(255, 0, 0)
    label.TextStrokeTransparency = 0.5
    label.Font = Enum.Font.SourceSansBold
    label.TextScaled = true
end

function RefreshESP()
    for _,v in pairs(Players:GetPlayers()) do
        if v ~= LP and Settings.EspPvP and not v.Character:FindFirstChild("PvP_ESP") then
            if v.Character and v.Character:FindFirstChild("HumanoidRootPart") and not v.Character:FindFirstChild("ForceField") then
                CreateESP(v)
            end
        end
    end
end

RunService.RenderStepped:Connect(function()
    if Settings.EspPvP then
        pcall(RefreshESP)
    end
end)

-- [11] HIỆU ỨNG KILL (RUNG/NỔ)
function KillEffect()
    if not Settings.KillEffect then return end
    spawn(function()
        local shock = Instance.new("Part", Workspace)
        shock.Anchored = true
        shock.Position = LP.Character.HumanoidRootPart.Position
        shock.Size = Vector3.new(5, 5, 5)
        shock.BrickColor = BrickColor.new("Bright red")
        shock.Material = Enum.Material.Neon
        shock.Shape = Enum.PartType.Ball
        shock.CanCollide = false
        game.Debris:AddItem(shock, 0.5)

        Camera.CameraType = Enum.CameraType.Scriptable
        for i = 1, 10 do
            Camera.CFrame = Camera.CFrame * CFrame.new(math.random(-1,1),math.random(-1,1),0)
            wait(0.02)
        end
        Camera.CameraType = Enum.CameraType.Custom
    end)
end

-- [12] AUTO DODGE
function AutoDodge()
    if not Settings.AutoDodge then return end
    for _, obj in pairs(Workspace:GetDescendants()) do
        if obj:IsA("Part") and obj.Name:lower():find("projectile") then
            local dist = (obj.Position - LP.Character.HumanoidRootPart.Position).Magnitude
            if dist <= Settings.AvoidSkillRange then
                LP.Character.HumanoidRootPart.CFrame = LP.Character.HumanoidRootPart.CFrame * CFrame.new(math.random(-15,15), 0, math.random(-15,15))
                wait(0.1)
            end
        end
    end
end

RunService.Heartbeat:Connect(function()
    if Settings.AutoDodge then
        AutoDodge()
    end
end)

-- [13] AUTO HAKI & OBSERVATION
RunService.Stepped:Connect(function()
    if Settings.AutoHaki then
        pcall(function()
            local Haki = LP.Character:FindFirstChild("HasBuso")
            if Haki and Haki.Value == false then
                game:GetService("ReplicatedStorage").Remotes.CommF_:InvokeServer("Buso")
            end
        end)
    end

    if Settings.AutoObservation then
        pcall(function()
            local Obs = LP.PlayerGui:FindFirstChild("ImageLabel")
            if not Obs then
                keypress(0x48) wait(0.2) keyrelease(0x48)
            end
        end)
    end
end)

-- [14] CHẾ ĐỘ FPS BOOST CỰC MẠNH
if Settings.FPSBoost then
    for _,v in pairs(game:GetDescendants()) do
        if v:IsA("Texture") or v:IsA("Decal") then
            v:Destroy()
        elseif v:IsA("ParticleEmitter") or v:IsA("Trail") then
            v.Enabled = false
        end
    end
    settings().Rendering.QualityLevel = Enum.QualityLevel.Level01
end

-- [15] KILL COUNT UPDATE
spawn(function()
    while true do
        wait(1)
        for _, tab in pairs(MainTab["SectionFrame"]:GetDescendants()) do
            if tab:IsA("TextLabel") and tab.Text:find("Kill Count:") then
                tab.Text = "Kill Count: " .. tostring(Settings.KillCount)
            end
        end
    end
end)

-- [16] THÊM ÂM THANH SAU KILL
function PlayKillSound()
    local sound = Instance.new("Sound", LP.Character.Head)
    sound.SoundId = "rbxassetid://138186576"
    sound.Volume = 5
    sound:Play()
    game.Debris:AddItem(sound, 3)
end

-- [17] TÍCH HỢP EFFECT SAU MỖI KILL
local oldKillCount = Settings.KillCount
RunService.Heartbeat:Connect(function()
    if Settings.KillEffect and Settings.KillCount > oldKillCount then
        KillEffect()
        PlayKillSound()
        oldKillCount = Settings.KillCount
    end
end)